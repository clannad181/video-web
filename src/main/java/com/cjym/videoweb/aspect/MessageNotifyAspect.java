package com.cjym.videoweb.aspect;

import com.cjym.videoweb.annotation.NotifyMessage;
import com.cjym.videoweb.model.entity.MessageInfo;
import com.cjym.videoweb.provider.CustomThreadPoolExecutorProvider;
import com.cjym.videoweb.service.MessageService;
import com.cjym.videoweb.service.NotifyMessageAssembler;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 消息通知 AOP 切面
 * <p>
 * 拦截 {@code @NotifyMessage} 注解的方法，方法正常返回后：
 * 1. 解析 target SpEL（默认 #result 返回值）得到消息数据来源对象；
 * 2. 交给 NotifyMessageAssembler 按消息类型组装为消息实体（同步阶段完成，避免跨线程访问事务/请求上下文）；
 * 3. 组装结果非空时，将消息落库 + WebSocket 推送提交线程池异步执行，不阻塞业务线程；
 * 4. 通知链路任何异常仅记录日志，绝不影响业务主流程。
 */
@Aspect
@Component
@Slf4j
public class MessageNotifyAspect {

    @Resource
    private MessageService messageService;

    @Resource
    private NotifyMessageAssembler notifyMessageAssembler;

    @Resource
    private CustomThreadPoolExecutorProvider customThreadPoolExecutorProvider;

    @Resource
    private ApplicationContext applicationContext;

    private static final SpelExpressionParser SPEL_PARSER = new SpelExpressionParser();

    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    /**
     * 方法正常返回后处理：@AfterReturning 保证业务失败（抛异常）时不发通知
     */
    @AfterReturning(pointcut = "@annotation(notifyMessage)", returning = "result")
    public void afterReturning(JoinPoint joinPoint, NotifyMessage notifyMessage, Object result) {
        try {
            EvaluationContext context = buildContext(joinPoint, result);
            // 额外发送条件不满足时静默跳过
            if (StringUtils.isNotEmpty(notifyMessage.condition())
                    && !Boolean.TRUE.equals(SPEL_PARSER.parseExpression(notifyMessage.condition()).getValue(context))) {
                return;
            }
            // 解析消息数据来源对象并同步组装消息快照
            Object target = SPEL_PARSER.parseExpression(notifyMessage.target()).getValue(context);
            MessageInfo message = notifyMessageAssembler.assemble(notifyMessage.type(), target, resolveArgs(joinPoint));
            if (message == null) {
                return;
            }
            // 异步阶段仅落库与推送
            customThreadPoolExecutorProvider.execute(() -> {
                try {
                    messageService.create(message);
                } catch (Exception e) {
                    log.warn("消息通知异步发送失败[type={}]", notifyMessage.type(), e);
                }
            });
        } catch (Exception e) {
            log.warn("消息通知注解解析失败，已跳过发送[method={}]", joinPoint.getSignature().toShortString(), e);
        }
    }

    /**
     * 构建 SpEL 上下文：注册 bean 解析器（支持 @beanName 引用）与 #参数名 / #result 变量
     */
    private EvaluationContext buildContext(JoinPoint joinPoint, Object result) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setBeanResolver(new BeanFactoryResolver(applicationContext));
        context.setVariable("result", result);
        resolveArgs(joinPoint).forEach(context::setVariable);
        return context;
    }

    /**
     * 解析方法参数名与参数值映射（参数名不可用时以 arg+下标 兜底）
     */
    private Map<String, Object> resolveArgs(JoinPoint joinPoint) {
        Map<String, Object> args = new LinkedHashMap<>();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = PARAMETER_NAME_DISCOVERER.getParameterNames(signature.getMethod());
        Object[] values = joinPoint.getArgs();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length && i < values.length; i++) {
                args.put(paramNames[i] != null ? paramNames[i] : "arg" + i, values[i]);
            }
        }
        return args;
    }
}
