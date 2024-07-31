package com.young.seckill.goods.application.cache.service;

import com.young.seckill.common.cache.model.SeckillBusinessCache;
import com.young.seckill.common.cache.service.SeckillCacheService;
import com.young.seckill.goods.domain.entity.SeckillGoods;

import java.util.List;

public interface SeckillGoodsListCacheService extends SeckillCacheService {

    /**
     * 根据活动ID和版本号获取商品列表缓存模型数据
     *
     * @param activityId 活动ID
     * @param version    版本号
     */
    SeckillBusinessCache<List<SeckillGoods>> getCacheGoods(Long activityId, Long version);

    /**
     * 尝试更新缓存数据
     *
     * @param activityId  活动ID
     * @param doubleCheck 双重检查锁
     */
    SeckillBusinessCache<List<SeckillGoods>> tryUpdateSeckillGoodsCacheByLock(Long activityId, boolean doubleCheck);
}
