package com.young.seckill.goods.application.cache.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.young.seckill.common.cache.distribute.DistributedCacheService;
import com.young.seckill.common.cache.local.LocalCacheService;
import com.young.seckill.common.cache.model.SeckillBusinessCache;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.lock.DistributedLock;
import com.young.seckill.common.lock.factory.DistributedLockFactory;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.common.utils.SystemClock;
import com.young.seckill.goods.application.cache.service.SeckillGoodsListCacheService;
import com.young.seckill.goods.domain.entity.SeckillGoods;
import com.young.seckill.goods.domain.repository.SeckillGoodsRepository;
import com.young.seckill.goods.domain.service.impl.SeckillGoodsDomainServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SeckillGoodsListCacheServiceImpl implements SeckillGoodsListCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillGoodsDomainServiceImpl.class);

    private final Lock localCacheUpdateLock = new ReentrantLock();

    private final ObjectMapper                                                      objectMapper;
    private final DistributedCacheService                                           distributedCacheService;
    private final SeckillGoodsRepository                                            seckillGoodsRepository;
    private final DistributedLockFactory                                            distributedLockFactory;
    private final LocalCacheService<Long, SeckillBusinessCache<List<SeckillGoods>>> localCacheService;

    public SeckillGoodsListCacheServiceImpl(ObjectMapper objectMapper,
                                            DistributedCacheService distributedCacheService,
                                            SeckillGoodsRepository seckillGoodsRepository,
                                            DistributedLockFactory distributedLockFactory,
                                            LocalCacheService<Long, SeckillBusinessCache<List<SeckillGoods>>> localCacheService) {
        this.objectMapper = objectMapper;
        this.localCacheService = localCacheService;
        this.seckillGoodsRepository = seckillGoodsRepository;
        this.distributedLockFactory = distributedLockFactory;
        this.distributedCacheService = distributedCacheService;
    }


    @Override
    public SeckillBusinessCache<List<SeckillGoods>> getCacheGoods(Long activityId, Long version) {
        String cacheKey = buildCacheKey(activityId);
        // 获取本地缓存
        SeckillBusinessCache<List<SeckillGoods>> seckillGoodsListCache = localCacheService.getIfPresent(cacheKey);

        if (seckillGoodsListCache != null) {
            // 传递的版本号为空，则直接返回本地缓存中的数据
            if (version == null) {
                LOGGER.info("SeckillGoodsListCache|命中本地缓存|{}", cacheKey);
                return seckillGoodsListCache;
            }

            // 缓存版本小于或等于缓存中版本号，则说明缓存中的数据比客户端的数据新，直接返回本地缓存中的数据
            if (version.compareTo(seckillGoodsListCache.getVersion()) <= 0) {
                LOGGER.info("SeckillGoodsListCache|命中本地缓存|{}", cacheKey);
                return seckillGoodsListCache;
            }

            // 传递的版本号大于缓存中的版本号，说明缓存中的数据比较落后，从分布式缓存获取数据并更新到本地缓存
            if (version.compareTo(seckillGoodsListCache.getVersion()) > 0) {
                return getDistributedCache(activityId);
            }
        }
        return getDistributedCache(activityId);
    }

    /**
     * 获取分布式缓存中的缓存数据
     */
    private SeckillBusinessCache<List<SeckillGoods>> getDistributedCache(Long activityId) {
        String cacheKey = buildCacheKey(activityId);
        LOGGER.info("SeckillGoodsListCache|读取分布式缓存|{}", cacheKey);

        SeckillBusinessCache<List<SeckillGoods>> seckillGoodsListCache = new SeckillBusinessCache<List<SeckillGoods>>().with(
                JACKSON.toObj(distributedCacheService.getString(cacheKey), new TypeReference<>() {
                }));

        if (seckillGoodsListCache == null) {
            seckillGoodsListCache = tryUpdateSeckillGoodsCacheByLock(activityId, true);
        }

        if (seckillGoodsListCache != null && !seckillGoodsListCache.isRetryLater()) {
            if (localCacheUpdateLock.tryLock()) {
                try {
                    localCacheService.put(activityId, seckillGoodsListCache);
                    LOGGER.info("SeckillGoodsListCache|本地缓存已经更新|{}", activityId);
                } finally {
                    localCacheUpdateLock.unlock();
                }
            }
        }
        return seckillGoodsListCache;
    }


    @Override
    public SeckillBusinessCache<List<SeckillGoods>> tryUpdateSeckillGoodsCacheByLock(Long activityId, boolean doubleCheck) {
        String cacheKey = buildCacheKey(activityId);
        LOGGER.info("SeckillGoodListCache|更新分布式缓存|{}", activityId);
        DistributedLock lock =
                distributedLockFactory.getDistributedLock(cacheKey.concat(SeckillConstants.DISTRIBUTED_GOODS_LOCK_SUFFIX));

        try {
            boolean isLockSuccess = lock.tryLock(2L, 5L, TimeUnit.MINUTES);
            if (!isLockSuccess) {
                return new SeckillBusinessCache<List<SeckillGoods>>().retryLater();
            }

            SeckillBusinessCache<List<SeckillGoods>> seckillGoodsListCache;

            if (doubleCheck) {
                seckillGoodsListCache = JACKSON.toObj(distributedCacheService.getString(cacheKey), new TypeReference<>() {
                });
                if (seckillGoodsListCache != null) {
                    return seckillGoodsListCache;
                }
            }

            List<SeckillGoods> seckillGoodsList = seckillGoodsRepository.getSeckillGoodsByActivityId(activityId);

            if (seckillGoodsList == null) {
                seckillGoodsListCache = new SeckillBusinessCache<List<SeckillGoods>>().notExist();
            } else {
                seckillGoodsListCache = new SeckillBusinessCache<List<SeckillGoods>>().with(seckillGoodsList)
                                                                                      .withVersion(SystemClock.millisClock().now());
            }

            distributedCacheService.put(cacheKey, objectMapper.writeValueAsString(seckillGoodsListCache),
                                        SeckillConstants.FIVE_MINUTES);

            LOGGER.info("SeckillGoodsListCache|分布式缓存已经更新|{}", activityId);
            return seckillGoodsListCache;
        } catch (InterruptedException | JsonProcessingException e) {
            LOGGER.error("SeckillGoodsListCache|分布式缓存更新失败|{}", activityId);
            return new SeckillBusinessCache<List<SeckillGoods>>().retryLater();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String buildCacheKey(Object key) {
        return SeckillConstants.getKey(SeckillConstants.SECKILL_GOODS_LIST_CACHE_KEY, key);
    }
}
