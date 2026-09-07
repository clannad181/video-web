package com.cjym.videoweb.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link NotifyMessage} 容器注解（@Repeatable 载体，支持同一方法声明多个消息通知）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NotifyMessages {

    NotifyMessage[] value();
}
