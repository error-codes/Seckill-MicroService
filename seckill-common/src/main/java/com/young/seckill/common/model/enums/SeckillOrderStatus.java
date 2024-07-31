package com.young.seckill.common.model.enums;

import lombok.Getter;

@Getter
public enum SeckillOrderStatus {

    CREATED(1),
    PAID(2),
    CANCELED(0),
    DELETED(-1);

    private final Integer code;

    SeckillOrderStatus(Integer code) {
        this.code = code;
    }

    public static boolean isCanceled(Integer stauts) {
        return CANCELED.getCode().equals(stauts);
    }

    public static boolean isDeleted(Integer status) {
        return DELETED.getCode().equals(status);
    }

}
