package com.zongce.comprehensive.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 拦截器统一 JSON 响应写出
 * <p>与业务接口返回结构一致：HTTP 200 + {@link Result#error(int, String)}（code 401/403/429 等），
 * 前端拦截器据此统一提示。</p>
 */
public final class ResultWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ResultWriter() {
    }

    /** 写出统一错误响应（HTTP 200 + JSON body） */
    public static void write(HttpServletResponse response, int code, String msg) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(Result.error(code, msg)));
    }
}
