package com.cjym.videoweb.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解 - 标记需要记录操作日志的 Controller 方法
 * <p>
 * 被标记的方法，AOP 切面会自动记录：
 * - 操作用户、模块、操作描述
 * - 请求参数、IP、URI
 * - 执行耗时、成功/失败状态
 * - 异常信息（失败时）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    /**
     * 所属模块，如 "用户管理"、"角色管理"、"文件管理"
     */
    String module() default "";

    /**
     * 操作描述，如 "新增用户"、"修改角色"、"上传文件"
     */
    String value() default "";
}
