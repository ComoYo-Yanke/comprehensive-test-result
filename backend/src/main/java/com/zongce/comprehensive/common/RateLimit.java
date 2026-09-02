package com.zongce.comprehensive.common;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流注解
 * <p>配合 {@code RateLimitInterceptor} 使用：按 IP 或按登录用户对某一接口在时间窗口内的请求次数进行限制，
 * 超过阈值返回 429 提示。作用在 Controller 方法或类上。</p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /** 限流维度：按 IP 还是按登录用户 */
    Scope scope() default Scope.USER;

    /** 时间窗口内允许的最大请求次数 */
    int limit() default 20;

    /** 时间窗口（秒） */
    int windowSeconds() default 60;

    /** 限流桶标识（区分同一接口不同维度场景） */
    String key() default "default";

    /** 超限提示文案 */
    String message() default "操作过于频繁，请稍后重试";

    enum Scope {
        /** 按客户端 IP 限流（适用于登录/注册等未登录接口） */
        IP,
        /** 按登录用户限流 */
        USER
    }
}
