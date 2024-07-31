package com.young.seckill.common.model.enums;

import lombok.Getter;

@Getter
public enum SeckillStockBucketStatus {

    ENABLED(1),
    DISABLED(0);

    private final Integer code;

    SeckillStockBucketStatus(Integer code) {
        this.code = code;
    }
}
