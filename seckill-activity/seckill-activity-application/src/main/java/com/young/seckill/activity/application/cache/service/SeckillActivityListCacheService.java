package com.young.seckill.activity.application.cache.service;


import com.young.seckill.activity.domain.entity.SeckillActivity;
import com.young.seckill.common.cache.model.SeckillBusinessCache;
import com.young.seckill.common.cache.service.SeckillCacheService;

import java.time.LocalDateTime;
import java.util.List;

public interface SeckillActivityListCacheService extends SeckillCacheService {

    /**
     * 根据状态和版本号获取活动列表缓存模型数据
     *
     * @param status  活动状态
     * @param version 版本号
     */
    SeckillBusinessCache<List<SeckillActivity>> getCacheActivities(Integer status, Long version);

    /**
     * 尝试更新缓存数据
     *
     * @param status      活动状态
     * @param doubleCheck 双重检查锁
     */
    SeckillBusinessCache<List<SeckillActivity>> tryUpdateSeckillActivityCacheByLock(Integer status, boolean doubleCheck);

    /**
     * 根据状态和版本号获取活动列表缓存模型数据【增加当前时间筛选条件】
     *
     * @param current 当前时间
     * @param status  活动状态
     * @param version 版本号
     */
    SeckillBusinessCache<List<SeckillActivity>> getCacheActivities(LocalDateTime current, Integer status, Long version);

    /**
     * 尝试更新缓存数据【增加当前时间筛选条件】
     *
     * @param current     当前时间
     * @param status      活动状态
     * @param doubleCheck 双重检查锁
     */
    SeckillBusinessCache<List<SeckillActivity>> tryUpdateSeckillActivityCacheByLock(LocalDateTime current,
                                                                                    Integer status,
                                                                                    boolean doubleCheck);
}
