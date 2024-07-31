package com.young.seckill.common.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RespResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 3063485411303518759L;

    private Integer code;
    private String  message;
    private T       data;

    public RespResult(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public RespResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public RespResult(RespCode resp) {
        this.code = resp.getCode();
        this.message = resp.getMessage();
    }

    public RespResult(RespCode resp, T data) {
        this.code = resp.getCode();
        this.message = resp.getMessage();
        this.data = data;
    }

    // 成功调用的方法
    public static <T> RespResult<T> success() {
        return new RespResult<>(RespCode.SUCCESS);
    }

    // 成功调用的方法
    public static <T> RespResult<T> success(T data) {
        return new RespResult<>(RespCode.SUCCESS, data);
    }

    // 失败调用的方法
    public static <T> RespResult<T> error(RespCode resp) {
        return new RespResult<>(resp);
    }

    // 失败调用的方法
    public static <T> RespResult<T> error(RespCode resp, T data) {
        return new RespResult<>(resp, data);
    }

    // 失败调用的方法
    public static <T> RespResult<T> error(Integer resp, String message) {
        return new RespResult<>(resp, message);
    }
}

