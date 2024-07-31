package com.young.seckill.common.model.enums;

import lombok.Getter;

@Getter
public enum SeckillRoleStatus {

    NORMAL(1),
    FREEZE(0);

    private final Integer code;

    SeckillRoleStatus(Integer code) {
        this.code = code;
    }

    public static boolean isNormal(Integer stauts) {
        return NORMAL.getCode().equals(stauts);
    }

}
