package com.zongce.comprehensive.config;

import com.zongce.comprehensive.common.ClientIpUtil;
import com.zongce.comprehensive.common.CurrentUser;
import com.zongce.comprehensive.common.RateLimit;
import com.zongce.comprehensive.common.ResultWriter;
import com.zongce.comprehensive.common.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

/**
 * 接口限流拦截器
 * <p>配合 {@link RateLimit} 注解使用：未标注的方法不拦截；标注后按 IP 或登录用户统计
 * 请求次数（Redis 固定窗口计数），超限返回 429。注册顺序在 JWT 拦截器之后，
 * 因此登录态已写入 {@link UserContext}；公开接口（登录/注册/验证码）由 IP 维度兜底。</p>
 */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 跨域预检直接放行
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        // 读取方法/类上的限流注解
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            rateLimit = handlerMethod.getBeanType().getAnnotation(RateLimit.class);
        }
        if (rateLimit == null) {
            return true;
        }

        // 确定计数维度
        String identity;
        if (rateLimit.scope() == RateLimit.Scope.IP) {
            identity = "ip:" + ClientIpUtil.resolve(request);
        } else {
            CurrentUser user = UserContext.getCurrentUser();
            if (user == null) {
                // 正常请求登录态已由 JWT 拦截器保证；未登录请求不在此维度限流
                return true;
            }
            identity = "user:" + user.getUserType() + ":" + user.getUserId();
        }

        // Redis 固定窗口计数：首次写入时设置过期时间
        String key = "rl:" + identity + ":" + rateLimit.key() + ":" + request.getMethod() + ":" + request.getRequestURI();
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(rateLimit.windowSeconds()));
        }
        if (count != null && count > rateLimit.limit()) {
            ResultWriter.write(response, 429, rateLimit.message());
            return false;
        }
        return true;
    }
}
