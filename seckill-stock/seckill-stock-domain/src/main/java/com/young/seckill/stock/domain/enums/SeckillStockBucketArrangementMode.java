package com.young.seckill.stock.domain.enums;

import lombok.Getter;

@Getter
public enum SeckillStockBucketArrangementMode {

    TOTAL(1),
    INCREMENTAL(2);

    private final Integer mode;

    SeckillStockBucketArrangementMode(Integer mode) {
        this.mode = mode;
    }

    public static boolean isTotalArrangementMode(Integer mode) {
        return TOTAL.mode.equals(mode);
    }

    public static boolean isIncrementalArrangementMode(Integer mode) {
        return INCREMENTAL.mode.equals(mode);
    }
}
