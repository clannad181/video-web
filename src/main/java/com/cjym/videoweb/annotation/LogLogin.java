package com.cjym.videoweb.annotation;

import java.lang.annotation.*;

/**
 * 登录日志注解 - 标记需要记录登录日志的方法
 * <p>
 * 既可标记在 Controller 层，也可标记在 Service 层，
 * AOP 切面会自动拦截并记录登录相关日志。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogLogin {

    /**
     * 登录类型描述，如 "图片验证码登录"、"邮箱验证码登录"
     */
    String value() default "";
}
