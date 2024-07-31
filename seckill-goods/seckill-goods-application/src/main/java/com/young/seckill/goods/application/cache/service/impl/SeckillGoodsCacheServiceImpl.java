package com.young.seckill.goods.application.cache.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.young.seckill.common.cache.distribute.DistributedCacheService;
import com.young.seckill.common.cache.local.LocalCacheService;
import com.young.seckill.common.cache.model.SeckillBusinessCache;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.lock.DistributedLock;
import com.young.seckill.common.lock.factory.DistributedLockFactory;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.common.utils.SystemClock;
import com.young.seckill.goods.application.cache.service.SeckillGoodsCacheService;
import com.young.seckill.goods.domain.entity.SeckillGoods;
import com.young.seckill.goods.domain.repository.SeckillGoodsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SeckillGoodsCacheServiceImpl implements SeckillGoodsCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillGoodsCacheServiceImpl.class);

    // 本地缓存更新锁
    private final Lock localCacheUpdateLock = new ReentrantLock();

    private final DistributedCacheService                                     distributedCacheService;
    private final SeckillGoodsRepository                                      seckillGoodsRepository;
    private final DistributedLockFactory                                      distributedLockFactory;
    private final LocalCacheService<Long, SeckillBusinessCache<SeckillGoods>> localCacheService;

    public SeckillGoodsCacheServiceImpl(DistributedCacheService distributedCacheService,
                                        DistributedLockFactory distributedLockFactory,
                                        SeckillGoodsRepository seckillGoodsRepository,
                                        LocalCacheService<Long, SeckillBusinessCache<SeckillGoods>> localCacheService) {
        this.localCacheService = localCacheService;
        this.seckillGoodsRepository = seckillGoodsRepository;
        this.distributedLockFactory = distributedLockFactory;
        this.distributedCacheService = distributedCacheService;
    }


    @Override
    public SeckillBusinessCache<SeckillGoods> getCacheGoods(Long goodsId, Long version) {
        // 从本地缓存获取数据
        SeckillBusinessCache<SeckillGoods> seckillGoodsCache = localCacheService.getIfPresent(goodsId);

        if (seckillGoodsCache != null) {
            // 传递的版本号为空，则直接返回本地缓存中的数据
            if (version == null) {
                LOGGER.info("SeckillGoodsCache|命中本地缓存|{}", goodsId);
                return seckillGoodsCache;
            }

            // 缓存版本小于或等于缓存中版本号，则说明缓存中的数据比客户端的数据新，直接返回本地缓存中的数据
            if (version.compareTo(seckillGoodsCache.getVersion()) <= 0) {
                LOGGER.info("SeckillGoodsCache|命中本地缓存|{}", goodsId);
                return seckillGoodsCache;
            }

            // 传递的版本号大于缓存中的版本号，说明缓存中的数据比较落后，从分布式缓存获取数据并更新到本地缓存
            if (version.compareTo(seckillGoodsCache.getVersion()) > 0) {
                return getDistributedCache(goodsId);
            }
        }

        return getDistributedCache(goodsId);
    }

    /**
     * 获取分布式缓存中的缓存数据
     */
    private SeckillBusinessCache<SeckillGoods> getDistributedCache(Long goodsId) {
        String cacheKey = buildCacheKey(goodsId);
        LOGGER.info("SeckillGoodsCache|读取分布式缓存|{}", goodsId);
        // 从分布式缓存中获取数据
        SeckillBusinessCache<SeckillGoods> seckillGoodsCache =
                JACKSON.toObj(distributedCacheService.getString(cacheKey), new TypeReference<>() {
                });

        // 分布式缓存中没有数据
        if (seckillGoodsCache == null) {
            // 尝试更新分布式缓存中的数据，注意的是只用一个线程去更新分布式缓存中的数据
            seckillGoodsCache = tryUpdateSeckillGoodsCacheByLock(goodsId, true);
        }

        // 获取的数据不为空，并不需要重试
        if (seckillGoodsCache != null && !seckillGoodsCache.isRetryLater()) {
            // 获取本地缓存更新锁，更新本地缓存
            if (localCacheUpdateLock.tryLock()) {
                try {
                    localCacheService.put(goodsId, seckillGoodsCache);
                    LOGGER.info("SeckillGoodsCache|本地缓存已经更新|{}", goodsId);
                } finally {
                    localCacheUpdateLock.unlock();
                }
            }
        }
        return seckillGoodsCache;
    }


    @Override
    public SeckillBusinessCache<SeckillGoods> tryUpdateSeckillGoodsCacheByLock(Long goodsId, boolean doubleCheck) {
        String cacheKey = buildCacheKey(goodsId);
        LOGGER.info("SeckillGoodsCache|更新分布式缓存|{}", goodsId);
        // 获取分布式锁，保证只有一个线程在更新分布式缓存
        DistributedLock lock =
                distributedLockFactory.getDistributedLock(cacheKey.concat(SeckillConstants.DISTRIBUTED_GOODS_LOCK_SUFFIX));

        try {
            boolean isLockSuccess = lock.tryLock(1L, 5L, TimeUnit.MINUTES);
            // 未获得分布式锁的线程快速返回，不占用系统资源
            if (!isLockSuccess) {
                return new SeckillBusinessCache<SeckillGoods>().retryLater();
            }

            SeckillBusinessCache<SeckillGoods> seckillGoodsCache;

            if (doubleCheck) {
                // 获取锁成功后，再次从缓存中获取数据，防止在高并发下多线程竞争的过程中或者在后续线程在等待 1 秒的过程中
                // 前面的线程释放锁，后续的线程获取锁成功后，再次更新分布式缓存数据
                seckillGoodsCache = JACKSON.toObj(distributedCacheService.getString(cacheKey), new TypeReference<>() {
                });
                if (seckillGoodsCache != null) {
                    return seckillGoodsCache;
                }
            }

            SeckillGoods seckillGoods = seckillGoodsRepository.getSeckillGoodsById(goodsId);

            if (seckillGoods == null) {
                seckillGoodsCache = new SeckillBusinessCache<SeckillGoods>().notExist();
            } else {
                seckillGoodsCache =
                        new SeckillBusinessCache<SeckillGoods>().with(seckillGoods).withVersion(SystemClock.millisClock().now());
            }

            // 将数据保存到分布式缓存种
            distributedCacheService.put(cacheKey, JACKSON.toJson(seckillGoodsCache), SeckillConstants.FIVE_MINUTES);
            LOGGER.info("SeckillGoodsCache|分布式缓存已经更新|{}", cacheKey);
            return seckillGoodsCache;
        } catch (InterruptedException e) {
            LOGGER.error("SeckillGoodsCache|分布式缓存更新失败|{}", cacheKey);
            return new SeckillBusinessCache<SeckillGoods>().retryLater();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String buildCacheKey(Object key) {
        return SeckillConstants.getKey(SeckillConstants.SECKILL_GOODS_CACHE_KEY, key);
    }
}
