package com.young.seckill.activity.application.cache.service;

import com.young.seckill.activity.domain.entity.SeckillActivity;
import com.young.seckill.common.cache.model.SeckillBusinessCache;
import com.young.seckill.common.cache.service.SeckillCacheService;

public interface SeckillActivityCacheService extends SeckillCacheService {

    /**
     * 根据活动ID和版本号获取活动列表缓存模型数据
     *
     * @param activityId 活动ID
     * @param version    版本号
     */
    SeckillBusinessCache<SeckillActivity> getCacheActivity(Long activityId, Long version);

    /**
     * 尝试更新缓存数据
     *
     * @param activityId  活动ID
     * @param doubleCheck 双重检查锁
     */
    SeckillBusinessCache<SeckillActivity> tryUpdateSeckillActivityCacheByLock(Long activityId, boolean doubleCheck);
}
