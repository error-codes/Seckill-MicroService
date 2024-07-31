package com.young.seckill.common.cache.model.base;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SeckillCommonCache {

    // 缓存数据是否存在
    protected boolean exist;

    // 缓存版本号
    protected Long version;

    // 稍后再试
    protected boolean retryLater;
}
