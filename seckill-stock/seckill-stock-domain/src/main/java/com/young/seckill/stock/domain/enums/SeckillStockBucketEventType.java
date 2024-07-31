package com.young.seckill.stock.domain.enums;

import lombok.Getter;

@Getter
public enum SeckillStockBucketEventType {

    DISABLED(0),
    ENABLED(1),
    ARRANGED(2);

    private final Integer code;

    SeckillStockBucketEventType(Integer code) {
        this.code = code;
    }
}
