package com.young.seckill.stock.application.cache.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.young.seckill.common.cache.distribute.DistributedCacheService;
import com.young.seckill.common.cache.local.LocalCacheService;
import com.young.seckill.common.cache.model.SeckillBusinessCache;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.exception.ExceptionChecker;
import com.young.seckill.common.lock.DistributedLock;
import com.young.seckill.common.lock.factory.DistributedLockFactory;
import com.young.seckill.common.response.RespCode;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.common.utils.SystemClock;
import com.young.seckill.stock.application.cache.SeckillStockBucketCacheService;
import com.young.seckill.stock.application.model.SeckillStockBucketDTO;
import com.young.seckill.stock.domain.entity.SeckillStockBucket;
import com.young.seckill.stock.domain.service.SeckillStockBucketDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SeckillStockBucketCacheServiceImpl implements SeckillStockBucketCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillStockBucketCacheServiceImpl.class);

    private final LocalCacheService<Long, SeckillBusinessCache<SeckillStockBucketDTO>> localCacheService;
    private final DistributedCacheService                                              distributedCacheService;
    private final DistributedLockFactory                                               distributedLockFactory;
    private final SeckillStockBucketDomainService                                      seckillStockBucketDomainService;
    private final Lock                                                                 localCacheUpdateLock = new ReentrantLock();

    public SeckillStockBucketCacheServiceImpl(LocalCacheService<Long, SeckillBusinessCache<SeckillStockBucketDTO>> localCacheService,
                                              DistributedCacheService distributedCacheService,
                                              DistributedLockFactory distributedLockFactory,
                                              SeckillStockBucketDomainService seckillStockBucketDomainService) {
        this.localCacheService = localCacheService;
        this.distributedCacheService = distributedCacheService;
        this.distributedLockFactory = distributedLockFactory;
        this.seckillStockBucketDomainService = seckillStockBucketDomainService;
    }

    @Override
    public SeckillBusinessCache<SeckillStockBucketDTO> getTotalStockBuckets(Long goodsId, Long version) {
        // 从本地缓存获取
        SeckillBusinessCache<SeckillStockBucketDTO> seckillStockBucketCache = localCacheService.getIfPresent(goodsId);
        if (seckillStockBucketCache != null) {
            // 版本号为空，则直接返回本地缓存中的数据
            if (seckillStockBucketCache.getVersion() == null) {
                LOGGER.info("seckillStockBucketCache|命中本地缓存|{}", goodsId);
                return seckillStockBucketCache;
            }
            // 传递的版本号小于等于缓存中的版本号，则说明缓存中的数据比客户端数据更新，直接返回本地缓存中的数据
            if (version.compareTo(seckillStockBucketCache.getVersion()) <= 0) {
                LOGGER.info("seckillStockBucketCache|命中本地缓存|{}", goodsId);
                return seckillStockBucketCache;
            }
            // 传递的版本号大于缓存中的版本号，说明缓存中数据比较落后，从分布式缓存获取数据并更新到本地缓存
            if (version.compareTo(seckillStockBucketCache.getVersion()) > 0) {
                return getDistributedCache(goodsId);
            }
        }
        return getDistributedCache(goodsId);
    }

    /**
     * 获取分布式缓存数据
     */
    private SeckillBusinessCache<SeckillStockBucketDTO> getDistributedCache(Long goodsId) {
        String cacheKey = buildCacheKey(goodsId);
        LOGGER.info("seckillStockBucketCache|读取分布式缓存|{}", goodsId);
        // 从分布式缓存中获取数据
        SeckillBusinessCache<SeckillStockBucketDTO> seckillStockCache =
                JACKSON.toObj(distributedCacheService.getString(cacheKey), new TypeReference<>() {});

        // 分布式缓存中没有数据
        if (seckillStockCache == null) {
            // 尝试更新分布式缓存中的数据，注意的是只用一个线程去更新分布式缓存中的数据
            seckillStockCache = tryUpdateSeckillStockBucketCacheByLock(goodsId, true);
        }

        // 获取的数据不为空，并且不需要重试
        if (seckillStockCache != null && !seckillStockCache.isRetryLater()) {
            // 获取本地锁，更新本地缓存
            if (localCacheUpdateLock.tryLock()) {
                try {
                    localCacheService.put(goodsId, seckillStockCache);
                    LOGGER.info("SeckillActivityCache|本地缓存已经更新|{}", goodsId);
                } finally {
                    localCacheUpdateLock.unlock();
                }
            }
        }
        return seckillStockCache;
    }

    @Override
    public SeckillBusinessCache<SeckillStockBucketDTO> tryUpdateSeckillStockBucketCacheByLock(Long goodsId, Boolean doubleCheck) {
        String cacheKey = buildCacheKey(goodsId);

        // 获取分布式锁，保证只有一个线程在更新分布式缓存
        DistributedLock distributedLock =
                distributedLockFactory.getDistributedLock(cacheKey.concat(SeckillConstants.DISTRIBUTED_STOCK_LOCK_SUFFIX));

        try {
            boolean isSuccess = distributedLock.tryLock(2L, 5L, TimeUnit.SECONDS);
            // 未获取到分布式锁的线程快速返回，不占用系统资源
            if (!isSuccess) {
                return new SeckillBusinessCache<SeckillStockBucketDTO>().retryLater();
            }
            SeckillBusinessCache<SeckillStockBucketDTO> seckillStockCache;
            if (doubleCheck) {
                // 获取锁成功后，再次从缓存中获取数据，防止高并发下多个线程争抢锁的过程中，后续线程在等待 1 秒的过程中
                // 前面的线程释放了锁，后续的线程获取锁成功后，再次更新分布式缓存数据
                seckillStockCache = JACKSON.toObj(distributedCacheService.getString(cacheKey), new TypeReference<>() {});

                if (seckillStockCache != null) {
                    return seckillStockCache;
                }
            }
            SeckillStockBucketDTO seckillStockBucketDTO = this.getSeckillStockBucketDTO(goodsId);
            if (seckillStockBucketDTO == null) {
                seckillStockCache = new SeckillBusinessCache<SeckillStockBucketDTO>().notExist();
            } else {
                seckillStockCache = new SeckillBusinessCache<SeckillStockBucketDTO>().with(seckillStockBucketDTO)
                                                                                     .withVersion(SystemClock.millisClock().now());
            }
            // 将数据保存到分布式缓存
            distributedCacheService.put(cacheKey, JACKSON.toJson(seckillStockCache), SeckillConstants.FIVE_MINUTES);
            LOGGER.info("seckillStockBucketCache|分布式缓存已经更新|{}", goodsId);
            return seckillStockCache;
        } catch (InterruptedException e) {
            LOGGER.error("seckillStockBucketCache|更新分布式缓存失败|{}", goodsId);
            return new SeckillBusinessCache<SeckillStockBucketDTO>().retryLater();
        } finally {
            distributedLock.unlock();
        }
    }

    private SeckillStockBucketDTO getSeckillStockBucketDTO(Long goodsId) {
        ExceptionChecker.throwAssertIfNullOrEmpty(goodsId, RespCode.PARAMS_INVALID);

        List<SeckillStockBucket> buckets = seckillStockBucketDomainService.getBucketByGoodsId(goodsId);

        ExceptionChecker.throwAssertIfNullOrEmpty(buckets, RespCode.ORDER_TOKEN_UNAVAILABLE);

        int availableStock = buckets.stream().mapToInt(SeckillStockBucket::getAvailableStock).sum();
        int totalStock = buckets.stream().mapToInt(SeckillStockBucket::getInitialStock).sum();
        return new SeckillStockBucketDTO(totalStock, availableStock, buckets);
    }


    @Override
    public String buildCacheKey(Object key) {
        return SeckillConstants.getKey(SeckillConstants.SECKILL_STOCK_CACHE_KEY, key);
    }

}
