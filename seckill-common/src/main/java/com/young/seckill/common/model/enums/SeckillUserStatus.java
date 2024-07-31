package com.young.seckill.common.model.enums;

import lombok.Getter;

@Getter
public enum SeckillUserStatus {

    NORMAL(1),
    FREEZE(-1);

    private final Integer code;

    SeckillUserStatus(Integer code) {
        this.code = code;
    }

    public static boolean isNormal(Integer stauts) {
        return NORMAL.getCode().equals(stauts);
    }

}
