package com.zongce.comprehensive.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口访问权限注解
 * <p>由 JWT 拦截器校验当前登录用户的类型与角色。</p>
 * <ul>
 *   <li>value：允许的用户类型（student / employee），空表示任意已登录用户。</li>
 *   <li>roles：允许的角色集合（数字），空表示不限制角色。</li>
 * </ul>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /** 允许的用户类型，空表示任意 */
    String value() default "";

    /** 允许的角色集合，空表示不限 */
    int[] roles() default {};
}
