package com.young.seckill.goods.application.cache.service;

import com.young.seckill.common.cache.model.SeckillBusinessCache;
import com.young.seckill.common.cache.service.SeckillCacheService;
import com.young.seckill.goods.domain.entity.SeckillGoods;

public interface SeckillGoodsCacheService extends SeckillCacheService {

    /**
     * 根据商品ID和版本号获取商品缓存模型数据
     *
     * @param goodsId 商品ID
     * @param version 版本号
     */
    SeckillBusinessCache<SeckillGoods> getCacheGoods(Long goodsId, Long version);

    /**
     * 尝试更新缓存数据
     *
     * @param goodsId     商品ID
     * @param doubleCheck 双重检查锁
     */
    SeckillBusinessCache<SeckillGoods> tryUpdateSeckillGoodsCacheByLock(Long goodsId, boolean doubleCheck);
}
