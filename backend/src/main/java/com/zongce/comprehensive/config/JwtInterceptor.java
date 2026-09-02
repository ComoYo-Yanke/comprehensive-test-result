package com.zongce.comprehensive.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zongce.comprehensive.common.CurrentUser;
import com.zongce.comprehensive.common.JwtUtil;
import com.zongce.comprehensive.common.RequireRole;
import com.zongce.comprehensive.common.Result;
import com.zongce.comprehensive.common.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器
 * <p>负责登录态校验与权限拦截：解析 token 写入上下文，校验 @RequireRole 权限。</p>
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 跨域预检请求直接放行
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 从请求头解析 token
        String token = resolveToken(request);
        if (token == null) {
            writeUnauthorized(response, "未登录或登录已过期");
            return false;
        }

        CurrentUser user;
        try {
            user = jwtUtil.parseToken(token);
        } catch (Exception e) {
            writeUnauthorized(response, "登录凭证无效或已过期");
            return false;
        }
        UserContext.set(user);

        // 权限校验
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole != null && !checkPermission(requireRole, user)) {
            writeForbidden(response);
            return false;
        }
        return true;
    }

    /** 校验用户类型与角色是否满足注解要求 */
    private boolean checkPermission(RequireRole requireRole, CurrentUser user) {
        String needType = requireRole.value();
        if (!needType.isEmpty() && !needType.equals(user.getUserType())) {
            return false;
        }
        int[] roles = requireRole.roles();
        if (roles.length > 0) {
            for (int role : roles) {
                if (user.getRole() != null && user.getRole() == role) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    /** 从 Authorization 头解析 Bearer token */
    private String resolveToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        // 兼容 URL 参数传递（用于 WebSocket 握手）
        String queryToken = request.getParameter("token");
        if (queryToken != null && !queryToken.isEmpty()) {
            return queryToken;
        }
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, msg)));
    }

    private void writeForbidden(HttpServletResponse response) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(403, "无权限访问")));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束清空上下文，防止线程复用导致数据串用
        UserContext.clear();
    }
}
