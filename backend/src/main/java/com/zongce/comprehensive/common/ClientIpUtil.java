package com.zongce.comprehensive.common;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 客户端 IP 解析工具
 * <p>生产环境经 Nginx 反向代理，需取 X-Forwarded-For 中最左侧的真实客户端 IP。</p>
 */
public final class ClientIpUtil {

    private ClientIpUtil() {
    }

    /** 解析请求来源 IP：优先 X-Forwarded-For 首项，其次 X-Real-IP，最后取远程地址 */
    public static String resolve(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int idx = forwarded.indexOf(',');
            String ip = (idx > 0 ? forwarded.substring(0, idx) : forwarded).trim();
            if (!ip.isEmpty()) {
                return ip;
            }
        }
        String real = request.getHeader("X-Real-IP");
        if (real != null && !real.isBlank()) {
            return real.trim();
        }
        return request.getRemoteAddr();
    }
}
