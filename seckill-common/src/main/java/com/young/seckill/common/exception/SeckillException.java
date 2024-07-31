package com.young.seckill.common.exception;

import com.young.seckill.common.response.RespCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class SeckillException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4378375535928407702L;

    private Integer code;

    public SeckillException(RespCode resp) {
        super(resp.getMessage());
        this.code = resp.getCode();
    }

    public SeckillException(String message) {
        super(message);
    }

}