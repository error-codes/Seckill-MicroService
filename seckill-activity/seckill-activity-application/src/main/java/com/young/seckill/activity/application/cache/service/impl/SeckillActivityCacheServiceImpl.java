package com.young.seckill.activity.application.cache.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.young.seckill.activity.application.cache.service.SeckillActivityCacheService;
import com.young.seckill.activity.domain.entity.SeckillActivity;
import com.young.seckill.activity.domain.repository.SeckillActivityRepository;
import com.young.seckill.common.cache.distribute.DistributedCacheService;
import com.young.seckill.common.cache.local.LocalCacheService;
import com.young.seckill.common.cache.model.SeckillBusinessCache;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.lock.DistributedLock;
import com.young.seckill.common.lock.factory.DistributedLockFactory;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.common.utils.SystemClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SeckillActivityCacheServiceImpl implements SeckillActivityCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillActivityCacheServiceImpl.class);

    // 本地缓存更新锁
    private final Lock localCacheUpdateLock = new ReentrantLock();

    private final DistributedCacheService                                        distributedCacheService;
    private final SeckillActivityRepository                                      seckillActivityRepository;
    private final DistributedLockFactory                                         distributedLockFactory;
    private final LocalCacheService<Long, SeckillBusinessCache<SeckillActivity>> localCacheService;

    public SeckillActivityCacheServiceImpl(DistributedCacheService distributedCacheService,
                                           DistributedLockFactory distributedLockFactory,
                                           SeckillActivityRepository seckillActivityRepository,
                                           LocalCacheService<Long, SeckillBusinessCache<SeckillActivity>> localCacheService) {
        this.localCacheService = localCacheService;
        this.distributedLockFactory = distributedLockFactory;
        this.distributedCacheService = distributedCacheService;
        this.seckillActivityRepository = seckillActivityRepository;
    }

    @Override
    public SeckillBusinessCache<SeckillActivity> getCacheActivity(Long activityId, Long version) {
        // 获取本地缓存
        SeckillBusinessCache<SeckillActivity> seckillActivityCache = localCacheService.getIfPresent(activityId);

        if (seckillActivityCache != null) {
            // 传递的版本号为空，则直接返回本地缓存中的数据
            if (version == null) {
                LOGGER.info("SeckillActivityCache|命中本地缓存|{}", activityId);
                return seckillActivityCache;
            }

            // 缓存版本小于或等于缓存中版本号，则说明缓存中的数据比客户端的数据新，直接返回本地缓存中的数据
            if (version.compareTo(seckillActivityCache.getVersion()) <= 0) {
                LOGGER.info("SeckillActivityCache|命中本地缓存|{}", activityId);
                return seckillActivityCache;
            }

            // 传递的版本号大于缓存中的版本号，说明缓存中的数据比较落后，从分布式缓存获取数据并更新到本地缓存
            if (version.compareTo(seckillActivityCache.getVersion()) > 0) {
                return getDistributedCache(activityId);
            }
        }
        // 从分布式缓存中获取数据，并设置到本地缓存中
        return getDistributedCache(activityId);
    }

    /**
     * 获取分布式缓存中的缓存数据
     */
    private SeckillBusinessCache<SeckillActivity> getDistributedCache(Long activityId) {
        String cacheKey = buildCacheKey(activityId);
        LOGGER.info("SeckillActivityCache|读取分布式缓存|{}", activityId);
        // 从分布式缓存中获取数据
        SeckillBusinessCache<SeckillActivity> seckillActivityCache =
                JACKSON.toObj(distributedCacheService.getString(cacheKey), new TypeReference<>() {});

        // 分布式缓存中没有数据
        if (seckillActivityCache == null) {
            // 尝试更新分布式缓存中的数据，注意的是只用一个线程去更新分布式缓存中的数据
            seckillActivityCache = tryUpdateSeckillActivityCacheByLock(activityId, true);
        }

        // 获取的数据不为空，并且不需要重试
        if (seckillActivityCache != null && !seckillActivityCache.isRetryLater()) {
            // 获取本地锁，更新本地缓存
            if (localCacheUpdateLock.tryLock()) {
                try {
                    localCacheService.put(activityId, seckillActivityCache);
                    LOGGER.info("SeckillActivityCache|本地缓存已经更新|{}", cacheKey);
                } finally {
                    localCacheUpdateLock.unlock();
                }
            }
        }
        return seckillActivityCache;
    }

    /**
     * 利用分布式锁保证只有一个线程去更新分布式缓存中的数据
     */
    @Override
    public SeckillBusinessCache<SeckillActivity> tryUpdateSeckillActivityCacheByLock(Long activityId, boolean doubleCheck) {
        String cacheKey = buildCacheKey(activityId);
        LOGGER.info("SeckillActivitiesCache|更新分布式缓存|{}", activityId);
        // 获取分布式锁
        DistributedLock lock =
                distributedLockFactory.getDistributedLock(cacheKey.concat(SeckillConstants.DISTRIBUTED_ACTIVITY_LOCK_SUFFIX));

        try {
            boolean isLockSuccess = lock.tryLock(1L, 5L, TimeUnit.MINUTES);
            // 未获取到分布式锁的线程快速返回，不占用系统资源
            if (!isLockSuccess) {
                return new SeckillBusinessCache<SeckillActivity>().retryLater();
            }
            SeckillBusinessCache<SeckillActivity> seckillActivityCache;
            if (doubleCheck) {
                // 获取锁成功后，再次从缓存中获取数据，防止高并发下多个线程争抢锁的过程中
                // 后续的线程再等待1秒的过程中，前面的线程释放了锁，后续的线程获取锁成功后再次更新分布式缓存数据
                seckillActivityCache = JACKSON.toObj(distributedCacheService.getString(cacheKey), new TypeReference<>() {});
                if (seckillActivityCache != null) {
                    return seckillActivityCache;
                }
            }

            SeckillActivity seckillActivity = seckillActivityRepository.getSeckillActivityById(activityId);

            if (seckillActivity == null) {
                seckillActivityCache = new SeckillBusinessCache<SeckillActivity>().notExist();
            } else {
                seckillActivityCache =
                        new SeckillBusinessCache<SeckillActivity>().with(seckillActivity).withVersion(SystemClock.millisClock().now());
            }

            // 将数据保存到分布式缓存
            distributedCacheService.put(cacheKey, JACKSON.toJson(seckillActivityCache), SeckillConstants.FIVE_MINUTES);
            LOGGER.info("SeckillActivitiesCache|分布式缓存已经更新|{}", activityId);
            return seckillActivityCache;
        } catch (InterruptedException e) {
            LOGGER.error("SeckillActivitiesCache|分布式缓存更新失败|{}", activityId);
            return new SeckillBusinessCache<SeckillActivity>().retryLater();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String buildCacheKey(Object key) {
        return SeckillConstants.getKey(SeckillConstants.SECKILL_ACTIVITY_CACHE_KEY, key);
    }
}

