package com.zongce.comprehensive.common;

/**
 * 当前登录用户上下文
 * <p>由 JWT 拦截器解析 token 后写入，业务代码通过 getCurrentUser() 获取当前用户。</p>
 */
public final class UserContext {

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser getCurrentUser() {
        return HOLDER.get();
    }

    /** 获取当前登录用户 id，未登录返回 null */
    public static Long getUserId() {
        CurrentUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    /** 获取当前登录用户类型（student/employee），未登录返回 null */
    public static String getUserType() {
        CurrentUser user = HOLDER.get();
        return user == null ? null : user.getUserType();
    }

    /** 清空上下文（请求结束时调用，防止线程池线程复用导致串数据） */
    public static void clear() {
        HOLDER.remove();
    }
}
