package com.cjym.videoweb.annotation;

import java.lang.annotation.*;

/**
 * 消息通知注解 - 声明式站内消息发送
 * <p>
 * 标注在业务方法上，方法正常返回后由 MessageNotifyAspect 切面统一处理：
 * - type 消息类型（引用 MessageConstant 的 TYPE_* 常量），决定消息的标题/内容模板与字段提取规则；
 * - target 为消息数据来源对象 SpEL，默认取方法返回值 #result（也可用 #参数名、@beanName）；
 * - condition 为额外发送条件 SpEL，满足才发送，为空表示恒真。
 * <p>
 * 切面按 type 将 target 对象交给消息组装器提取接收者与消息内容，再提交线程池异步落库与推送，
 * 不阻塞业务线程；通知链路任何异常仅记录日志，不影响业务主流程。
 * <p>
 * 注解可重复使用（@Repeatable），同一方法需要发送多类通知时（如举报受理同时通知 UP 主与举报人）
 * 可声明多个 @NotifyMessage，切面按声明顺序逐个处理。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(NotifyMessages.class)
public @interface NotifyMessage {

    /**
     * 消息类型，取值见 MessageConstant 的 TYPE_* 常量
     */
    int type();

    /**
     * 消息数据来源对象 SpEL，默认取方法返回值 #result
     */
    String target() default "#result";

    /**
     * 额外发送条件 SpEL（满足才发送），为空表示恒真
     */
    String condition() default "";
}
