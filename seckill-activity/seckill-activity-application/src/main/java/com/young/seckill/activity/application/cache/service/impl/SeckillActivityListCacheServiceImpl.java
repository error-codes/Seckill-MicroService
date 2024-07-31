package com.young.seckill.activity.application.cache.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.young.seckill.activity.application.cache.service.SeckillActivityListCacheService;
import com.young.seckill.activity.domain.entity.SeckillActivity;
import com.young.seckill.activity.domain.service.SeckillActivityDomainService;
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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Service
public class SeckillActivityListCacheServiceImpl implements SeckillActivityListCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillActivityListCacheServiceImpl.class);

    // 本地缓存更新锁
    private final Lock localCacheUpdateLock = new ReentrantLock();

    private final DistributedCacheService                                              distributedCacheService;
    private final SeckillActivityDomainService                                         seckillActivityDomainService;
    private final DistributedLockFactory                                               distributedLockFactory;
    private final LocalCacheService<Long, SeckillBusinessCache<List<SeckillActivity>>> localCacheService;

    public SeckillActivityListCacheServiceImpl(DistributedCacheService distributedCacheService,
                                               SeckillActivityDomainService seckillActivityDomainService,
                                               DistributedLockFactory distributedLockFactory,
                                               LocalCacheService<Long, SeckillBusinessCache<List<SeckillActivity>>> localCacheService) {
        this.localCacheService = localCacheService;
        this.distributedLockFactory = distributedLockFactory;
        this.distributedCacheService = distributedCacheService;
        this.seckillActivityDomainService = seckillActivityDomainService;
    }


    @Override
    public SeckillBusinessCache<List<SeckillActivity>> getCacheActivities(Integer status, Long version) {
        // 获取本地缓存
        SeckillBusinessCache<List<SeckillActivity>> seckillActivityListCache = localCacheService.getIfPresent(status.longValue());

        if (seckillActivityListCache != null) {
            // 传递的版本号为空，则直接返回本地缓存中的数据
            if (version == null) {
                LOGGER.info("SeckillActivityListCache|命中本地缓存|{}", status);
                return seckillActivityListCache;
            }

            // 缓存版本小于或等于缓存中版本号，则说明缓存中的数据比客户端的数据新，直接返回本地缓存中的数据
            if (version.compareTo(seckillActivityListCache.getVersion()) <= 0) {
                LOGGER.info("SeckillActivityListCache|命中本地缓存|{}", status);
                return seckillActivityListCache;
            }

            // 传递的版本号大于缓存中的版本号，说明缓存中的数据比较落后，从分布式缓存获取数据并更新到本地缓存
            if (version.compareTo(seckillActivityListCache.getVersion()) > 0) {
                return getDistributedCache(status);
            }
        }
        return getDistributedCache(status);
    }

    /**
     * 获取分布式缓存中的缓存数据
     */
    private SeckillBusinessCache<List<SeckillActivity>> getDistributedCache(Integer status) {
        String cacheKey = buildCacheKey(status);
        LOGGER.info("SeckillActivityListCache|读取分布式缓存|{}", status);
        SeckillBusinessCache<List<SeckillActivity>> seckillActivityListCache = new SeckillBusinessCache<List<SeckillActivity>>().with(
                JACKSON.toObj(distributedCacheService.getString(cacheKey), new TypeReference<>() {
                }));

        if (seckillActivityListCache == null) {
            seckillActivityListCache = tryUpdateSeckillActivityCacheByLock(status, true);
        }

        if (seckillActivityListCache != null && !seckillActivityListCache.isRetryLater()) {
            if (localCacheUpdateLock.tryLock()) {
                try {
                    localCacheService.put(status.longValue(), seckillActivityListCache);
                    LOGGER.info("SeckillActivityListCache|本地缓存已经更新|{}", status);
                } finally {
                    localCacheUpdateLock.unlock();
                }
            }
        }
        return seckillActivityListCache;
    }

    @Override
    public SeckillBusinessCache<List<SeckillActivity>> tryUpdateSeckillActivityCacheByLock(Integer status, boolean doubleCheck) {
        String cacheKey = buildCacheKey(status);
        LOGGER.info("SeckillActivityListCache|更新分布式缓存|{}", status);
        DistributedLock lock =
                distributedLockFactory.getDistributedLock(cacheKey.concat(SeckillConstants.DISTRIBUTED_ACTIVITY_LOCK_SUFFIX));

        try {
            boolean isLockSuccess = lock.tryLock(1L, 5L, TimeUnit.MINUTES);
            if (!isLockSuccess) {
                return new SeckillBusinessCache<List<SeckillActivity>>().retryLater();
            }
            SeckillBusinessCache<List<SeckillActivity>> seckillActivityListCache;

            if (doubleCheck) {
                // 获取锁成功后，再次从缓存中获取数据，防止高并发下多个线程争抢锁的过程中，后续的线程再等待1秒的过程中
                // 前面的线程释放了锁，后续的线程获取锁成功后再次更新分布式缓存数据
                seckillActivityListCache =
                        JACKSON.toObj(distributedCacheService.getString(buildCacheKey(status)), new TypeReference<>() {
                        });
                if (seckillActivityListCache != null) {
                    return seckillActivityListCache;
                }
            }

            List<SeckillActivity> seckillActivityList = seckillActivityDomainService.getSeckillActivityList(status);

            if (seckillActivityList == null) {
                seckillActivityListCache = new SeckillBusinessCache<List<SeckillActivity>>().notExist();
            } else {
                seckillActivityListCache = new SeckillBusinessCache<List<SeckillActivity>>().with(seckillActivityList)
                                                                                            .withVersion(
                                                                                                    SystemClock.millisClock().now());
            }

            distributedCacheService.put(cacheKey, JACKSON.toJson(seckillActivityListCache), SeckillConstants.FIVE_MINUTES);

            LOGGER.info("SeckillActivityListCache|分布式缓存已经更新|{}", status);
            return seckillActivityListCache;
        } catch (InterruptedException e) {
            LOGGER.error("SeckillActivityListCache|分布式缓存更新失败|{}", status);
            return new SeckillBusinessCache<List<SeckillActivity>>().retryLater();
        } finally {
            lock.unlock();
        }
    }


    @Override
    public SeckillBusinessCache<List<SeckillActivity>> getCacheActivities(LocalDateTime current, Integer status, Long version) {
        // 获取本地缓存 Key 值
        long localKey = current.toEpochSecond(ZoneOffset.UTC) + status.longValue();
        // 获取本地缓存
        SeckillBusinessCache<List<SeckillActivity>> seckillActivityListCache = localCacheService.getIfPresent(localKey);

        if (seckillActivityListCache != null) {
            if (version == null) {
                LOGGER.info("SeckillActivityListCache|命中本地缓存|{}", localKey);
                return seckillActivityListCache;
            }

            // 缓存版本小于或等于缓存中版本号
            if (version.compareTo(seckillActivityListCache.getVersion()) <= 0) {
                LOGGER.info("SeckillActivityListCache|命中本地缓存|{}", localKey);
                return seckillActivityListCache;
            }

            if (version.compareTo(seckillActivityListCache.getVersion()) > 0) {
                return getDistributedCache(current, status);
            }
        }
        return getDistributedCache(current, status);
    }

    /**
     * 获取分布式缓存中的缓存数据
     */
    private SeckillBusinessCache<List<SeckillActivity>> getDistributedCache(LocalDateTime current, Integer status) {
        // 获取本地缓存 Key 值
        long localKey = current.toEpochSecond(ZoneOffset.UTC) + status.longValue();
        LOGGER.info("SeckillActivityListCache|读取分布式缓存|{}", localKey);
        SeckillBusinessCache<List<SeckillActivity>> seckillActivityListCache = new SeckillBusinessCache<List<SeckillActivity>>().with(
                JACKSON.toObj(distributedCacheService.getString(buildCacheKey(localKey)), new TypeReference<>() {
                }));

        if (seckillActivityListCache.getData() == null) {
            seckillActivityListCache = tryUpdateSeckillActivityCacheByLock(current, status, true);
        }

        if (seckillActivityListCache.getData() != null && !seckillActivityListCache.isRetryLater()) {
            if (localCacheUpdateLock.tryLock()) {
                try {
                    localCacheService.put(localKey, seckillActivityListCache);
                    LOGGER.info("SeckillActivityListCache|本地缓存已经更新|{}", status);
                } finally {
                    localCacheUpdateLock.unlock();
                }
            }
        }
        return seckillActivityListCache;
    }

    @Override
    public SeckillBusinessCache<List<SeckillActivity>> tryUpdateSeckillActivityCacheByLock(LocalDateTime current,
                                                                                           Integer status,
                                                                                           boolean doubleCheck) {
        // 获取本地缓存 Key 值
        long localKey = current.toEpochSecond(ZoneOffset.UTC) + status.longValue();
        LOGGER.info("SeckillActivityListCache|更新分布式缓存|{}", localKey);
        DistributedLock lock = distributedLockFactory.getDistributedLock(
                buildCacheKey(localKey).concat(SeckillConstants.DISTRIBUTED_ACTIVITY_LOCK_SUFFIX));

        try {
            boolean isLockSuccess = lock.tryLock(1L, 5L, TimeUnit.MINUTES);
            if (!isLockSuccess) {
                return new SeckillBusinessCache<List<SeckillActivity>>().retryLater();
            }
            SeckillBusinessCache<List<SeckillActivity>> seckillActivityListCache;

            if (doubleCheck) {
                // 获取锁成功后，再次从缓存中获取数据，防止高并发下多个线程争抢锁的过程中，后续的线程再等待1秒的过程中
                // 前面的线程释放了锁，后续的线程获取锁成功后再次更新分布式缓存数据
                seckillActivityListCache =
                        JACKSON.toObj(distributedCacheService.getString(buildCacheKey(status)), new TypeReference<>() {
                        });
                if (seckillActivityListCache != null) {
                    return seckillActivityListCache;
                }
            }

            List<SeckillActivity> seckillActivityList =
                    seckillActivityDomainService.getSeckillActivityListBetweenStartTimeAndEndTime(current, status);

            if (seckillActivityList == null) {
                seckillActivityListCache = new SeckillBusinessCache<List<SeckillActivity>>().notExist();
            } else {
                seckillActivityListCache = new SeckillBusinessCache<List<SeckillActivity>>().with(seckillActivityList)
                                                                                            .withVersion(
                                                                                                    SystemClock.millisClock().now());
            }

            distributedCacheService.put(buildCacheKey(localKey), JACKSON.toJson(seckillActivityListCache),
                                        SeckillConstants.FIVE_MINUTES);

            LOGGER.info("SeckillActivityListCache|分布式缓存已经更新|{}", localKey);
            return seckillActivityListCache;
        } catch (InterruptedException e) {
            LOGGER.error("SeckillActivityListCache|分布式缓存更新失败|{}", localKey);
            return new SeckillBusinessCache<List<SeckillActivity>>().retryLater();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String buildCacheKey(Object key) {
        return SeckillConstants.getKey(SeckillConstants.SECKILL_ACTIVITY_LIST_CACHE_KEY, key);
    }
}
