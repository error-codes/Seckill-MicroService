package com.young.seckill.common.model.enums;

import lombok.Getter;

@Getter
public enum SeckillGoodsStatus {

    PUBLISHED(0),
    ONLINE(1),
    OFFLINE(-1);

    private final Integer code;

    SeckillGoodsStatus(Integer code) {
        this.code = code;
    }

    public static boolean isOffline(Integer stauts) {
        return OFFLINE.getCode().equals(stauts);
    }

    public static boolean isOnline(Integer status) {
        return ONLINE.getCode().equals(status);
    }

}
