package com.zongce.comprehensive.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果
 * <p>格式：{code, msg, data}，code=1 成功，code=0 失败。</p>
 */
@Data
public class Result<T> implements Serializable {

    /** 状态码：1 成功，0 失败 */
    private Integer code;
    /** 提示信息 */
    private String msg;
    /** 数据体 */
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(1, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(1, "操作成功", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(1, msg, data);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(0, msg, null);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }
}
