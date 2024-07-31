package com.young.seckill.common.exception;

import com.young.seckill.common.response.RespCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class AssertException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4378375535928417702L;

    private Integer code;

    public AssertException(String message) {
        super(message);
    }

    public AssertException(RespCode respCode) {
        super(respCode.getMessage());
        this.code = respCode.getCode();
    }

}
