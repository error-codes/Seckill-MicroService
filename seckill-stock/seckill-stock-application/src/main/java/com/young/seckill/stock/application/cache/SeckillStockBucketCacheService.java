package com.young.seckill.stock.application.cache;

import com.young.seckill.common.cache.model.SeckillBusinessCache;
import com.young.seckill.common.cache.service.SeckillCacheService;
import com.young.seckill.stock.application.model.SeckillStockBucketDTO;

public interface SeckillStockBucketCacheService extends SeckillCacheService {

    /**
     * 获取当前商品库存分桶信息
     *
     * @param goodsId 商品ID
     * @param version 版本号
     */
    SeckillBusinessCache<SeckillStockBucketDTO> getTotalStockBuckets(Long goodsId, Long version);

    /**
     * 更新当前商品库存分桶信息
     *
     * @param goodsId     商品ID
     * @param doubleCheck 双重检查锁
     */
    SeckillBusinessCache<SeckillStockBucketDTO> tryUpdateSeckillStockBucketCacheByLock(Long goodsId, Boolean doubleCheck);
}
