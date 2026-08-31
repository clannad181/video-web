package com.cjym.videoweb.aspect;

import com.cjym.videoweb.annotation.LogOperation;
import com.cjym.videoweb.model.entity.OperationLog;
import com.cjym.videoweb.result.R;
import com.cjym.videoweb.service.OperationLogService;
import com.cjym.videoweb.utils.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

/**
 * 运行日志 AOP 切面
 * <p>
 * 两大职责，与现有全局异常体系无缝融合：
 * 1. {@code @Around("@annotation(logOperation)")} → 拦截 @LogOperation 注解的方法，记录完整操作日志
 * 2. {@code @Around("globExceptionHandler()")} → 织入 GlobExceptionHandler，结合原有的全局异常处理逻辑，记录系统异常日志
 */
@Aspect
@Component
@Slf4j
public class OperationLogAspect {

    @Resource
    private OperationLogService operationLogService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // ==================== 1. 操作日志记录（所有 Controller public 方法） ====================

    /**
     * 切点：所有 controller 包下的 public 方法
     * module/operation 优先级：@LogOperation > Swagger @Tag/@Operation 兜底
     */
    @Pointcut("execution(public * com.cjym.videoweb.controller..*.*(..))")
    public void controllerMethod() {
    }

    @Around("controllerMethod()")
    public Object aroundOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        OperationLog operationLog = new OperationLog();

        // 获取方法上的 @LogOperation（可能为 null）
        LogOperation logOperation = getLogOperationAnnotation(joinPoint);

        try {
            fillRequestContext(operationLog, joinPoint, logOperation);
            Object result = joinPoint.proceed();
            // 根据返回值 R 的 code 区分成功 / 业务异常
            if (result instanceof R<?> r) {
                operationLog.setResultStatus(r.getCode() != null && r.getCode() == 200 ? 2 : 1);
            } else {
                operationLog.setResultStatus(2);
            }
            operationLog.setExecutionTime(System.currentTimeMillis() - startTime);
            operationLog.setLogType(1);
            operationLogService.saveLog(operationLog);
            return result;
        } catch (Throwable e) {
            // 不在此处记录异常日志，交由 GlobExceptionHandler 统一以 logType=0 记录
            throw e;
        }
    }

    /**
     * 从切点方法上获取 @LogOperation 注解（可能为 null）
     */
    private LogOperation getLogOperationAnnotation(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            return method.getAnnotation(LogOperation.class);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== 2. 全局异常日志（织入 GlobExceptionHandler） ====================

    /**
     * 切点：GlobExceptionHandler 中所有异常处理方法
     * 直接融合现有全局异常体系，无需修改原代码
     */
    @Pointcut("execution(* com.cjym.videoweb.handler.exception.GlobExceptionHandler.*(..))")
    public void globExceptionHandler() {
    }

    /**
     * 环绕通知：围绕 GlobExceptionHandler 的异常处理方法，
     * 在原有的异常处理逻辑前后记录系统异常日志（logType=0）
     */
    @Around("globExceptionHandler()")
    public Object aroundGlobExceptionHandler(ProceedingJoinPoint joinPoint) throws Throwable {
        Throwable exception = extractException(joinPoint);
        if (exception != null) {
            // 日志记录失败不能阻断 GlobExceptionHandler 执行，必须用 try-catch 保护
            try {
                OperationLog operationLog = new OperationLog();
                fillRequestContext(operationLog, joinPoint, null);
                resolveOriginalControllerInfo(operationLog, exception);
                operationLog.setResultStatus(0);
                operationLog.setLogType(0);
                operationLog.setErrorMessage(truncate(exception.getMessage(), 2000));
                operationLog.setErrorStackTrace(truncate(getStackTrace(exception), 5000));
                operationLogService.saveLog(operationLog);
            } catch (Exception e) {
                log.error("记录全局异常日志失败，已降级放行", e);
            }
        }
        return joinPoint.proceed();
    }

    // ==================== 通用方法 ====================

    /**
     * 从 GlobExceptionHandler 方法参数中提取异常对象
     */
    private Throwable extractException(ProceedingJoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Throwable) {
                return (Throwable) arg;
            }
        }
        return null;
    }

    /**
     * 从原始 HTTP 请求中还原报错时真正的 Controller 信息：
     * module（@Tag）、operation（@Operation summary）、method（类名.方法名），
     * 并清除 GlobExceptionHandler 参数序列化产生的无效 requestParams。
     */
    private void resolveOriginalControllerInfo(OperationLog operationLog, Throwable exception) {
        // 清除 GlobExceptionHandler 参数（异常对象）序列化的无效 JSON
        operationLog.setRequestParams("");
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Object handler = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE);
                if (handler instanceof HandlerMethod handlerMethod) {
                    Class<?> controllerClass = handlerMethod.getBeanType();
                    // Controller 类上的 @Tag → module
                    Tag classTag = controllerClass.getAnnotation(Tag.class);
                    if (classTag != null && StringUtils.isNotEmpty(classTag.name())) {
                        operationLog.setModule(classTag.name());
                    }
                    // Controller 方法上的 @Operation summary → operation
                    Operation swaggerOp = handlerMethod.getMethodAnnotation(Operation.class);
                    if (swaggerOp != null && StringUtils.isNotEmpty(swaggerOp.summary())) {
                        operationLog.setOperation(swaggerOp.summary());
                    }
                    // 真实方法名
                    operationLog.setMethod(controllerClass.getSimpleName() + "." + handlerMethod.getMethod().getName());
                }
            }
        } catch (Exception e) {
            log.warn("还原原始Controller信息失败", e);
        }
        // 兜底：module 或 operation 仍为空时
        if (StringUtils.isEmpty(operationLog.getModule())) {
            operationLog.setModule("系统异常");
        }
        if (StringUtils.isEmpty(operationLog.getOperation())) {
            operationLog.setOperation(exception.getClass().getSimpleName());
        }
    }

    private void fillRequestContext(OperationLog operationLog, Object joinPoint, LogOperation annotation) {
        // 1. module/operation：@LogOperation 优先，Swagger @Tag/@Operation 兜底
        if (annotation != null) {
            operationLog.setModule(annotation.module());
            operationLog.setOperation(annotation.value());
        } else if (joinPoint instanceof ProceedingJoinPoint pjp) {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            resolveModuleFromSwagger(operationLog, signature);
            resolveOperationFromSwagger(operationLog, signature);
        } else if (joinPoint instanceof JoinPoint jp) {
            MethodSignature signature = (MethodSignature) jp.getSignature();
            resolveModuleFromSwagger(operationLog, signature);
            resolveOperationFromSwagger(operationLog, signature);
        }

        if (joinPoint instanceof ProceedingJoinPoint pjp) {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            operationLog.setMethod(signature.getDeclaringType().getSimpleName() + "." + signature.getName());
            operationLog.setRequestParams(serializeArgs(pjp.getArgs()));
        } else if (joinPoint instanceof JoinPoint jp) {
            MethodSignature signature = (MethodSignature) jp.getSignature();
            operationLog.setMethod(signature.getDeclaringType().getSimpleName() + "." + signature.getName());
            operationLog.setRequestParams(serializeArgs(jp.getArgs()));
        }

        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operationLog.setRequestUri(request.getRequestURI());
                operationLog.setRequestMethod(request.getMethod());
                operationLog.setIpAddress(getClientIp(request));
                operationLog.setSystemType(resolveSystemType(request));
                operationLog.setUserId(resolveUserId(request));
            }
        } catch (Exception e) {
            log.error("填充请求上下文异常", e);
        }
    }

    private Long resolveUserId(HttpServletRequest request) {
        String token = request.getHeader("authorization");
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        token = token.trim();
        if (JWTUtil.validateToken(token)) {
            return JWTUtil.getUserId(token);
        }
        return null;
    }

    private Integer resolveSystemType(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri != null && uri.contains("/admin/")) {
            return 1;
        }
        return 0;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        try {
            Object[] filtered = java.util.Arrays.stream(args)
                    .filter(a -> !(a instanceof HttpServletRequest)
                            && !(a instanceof jakarta.servlet.http.HttpServletResponse))
                    .toArray();
            if (filtered.length == 0) {
                return "";
            }
            if (filtered.length == 1) {
                return truncate(OBJECT_MAPPER.writeValueAsString(filtered[0]), 3000);
            }
            return truncate(OBJECT_MAPPER.writeValueAsString(filtered), 3000);
        } catch (Exception e) {
            return "[参数序列化失败]";
        }
    }

    private String getStackTrace(Throwable e) {
        if (e == null) {
            return null;
        }
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * 从 Swagger @Tag 读取模块名（兜底）
     */
    private void resolveModuleFromSwagger(OperationLog operationLog, MethodSignature signature) {
        try {
            // 方法所在类上的 @Tag
            Class<?> declaringClass = signature.getDeclaringType();
            Tag classTag = declaringClass.getAnnotation(Tag.class);
            if (classTag != null && StringUtils.isNotEmpty(classTag.name())) {
                operationLog.setModule(classTag.name());
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 从 Swagger @Operation 读取操作描述（兜底）
     */
    private void resolveOperationFromSwagger(OperationLog operationLog, MethodSignature signature) {
        try {
            Method method = signature.getMethod();
            Operation swaggerOp = method.getAnnotation(Operation.class);
            if (swaggerOp != null && StringUtils.isNotEmpty(swaggerOp.summary())) {
                operationLog.setOperation(swaggerOp.summary());
            }
        } catch (Exception ignored) {
        }
    }

    private String truncate(String str, int maxLen) {
        if (str == null) {
            return null;
        }
        return str.length() > maxLen ? str.substring(0, maxLen) : str;
    }
}
