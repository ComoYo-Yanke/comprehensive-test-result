package com.zongce.comprehensive.common;

/**
 * 业务异常
 * <p>业务逻辑校验不通过时抛出，由全局异常处理器统一转为 {code:0, msg} 响应。</p>
 */
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 0;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
