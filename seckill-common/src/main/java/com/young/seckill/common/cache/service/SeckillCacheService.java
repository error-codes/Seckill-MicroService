package com.young.seckill.common.cache.service;

public interface SeckillCacheService {

    /**
     * 构建缓存 Key
     */
    String buildCacheKey(Object key);
}