package com.young.seckill.stock.application.service.impl;

import com.young.seckill.common.cache.distribute.DistributedCacheService;
import com.young.seckill.common.cache.model.SeckillBusinessCache;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.exception.ExceptionChecker;
import com.young.seckill.common.exception.SeckillException;
import com.young.seckill.common.lock.DistributedLock;
import com.young.seckill.common.lock.factory.DistributedLockFactory;
import com.young.seckill.common.model.enums.SeckillStockBucketStatus;
import com.young.seckill.common.response.RespCode;
import com.young.seckill.stock.application.cache.SeckillStockBucketCacheService;
import com.young.seckill.stock.application.model.SeckillStockBucketDTO;
import com.young.seckill.stock.application.service.SeckillStockBucketArrangementService;
import com.young.seckill.stock.domain.entity.SeckillStockBucket;
import com.young.seckill.stock.domain.enums.SeckillStockBucketArrangementMode;
import com.young.seckill.stock.domain.service.SeckillStockBucketDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class SeckillStockBucketArrangementServiceImpl implements SeckillStockBucketArrangementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillStockBucketArrangementServiceImpl.class);


    private final TransactionDefinition           transactionDefinition;
    private final DistributedLockFactory          distributedLockFactory;
    private final DistributedCacheService         distributedCacheService;
    private final DataSourceTransactionManager    dataSourceTransactionManager;
    private final SeckillStockBucketCacheService  seckillStockBucketCacheService;
    private final SeckillStockBucketDomainService seckillStockBucketDomainService;

    public SeckillStockBucketArrangementServiceImpl(TransactionDefinition transactionDefinition,
                                                    DistributedLockFactory distributedLockFactory,
                                                    DistributedCacheService distributedCacheService,
                                                    DataSourceTransactionManager dataSourceTransactionManager,
                                                    SeckillStockBucketCacheService seckillStockBucketCacheService,
                                                    SeckillStockBucketDomainService seckillStockBucketDomainService) {
        this.transactionDefinition = transactionDefinition;
        this.distributedLockFactory = distributedLockFactory;
        this.distributedCacheService = distributedCacheService;
        this.dataSourceTransactionManager = dataSourceTransactionManager;
        this.seckillStockBucketCacheService = seckillStockBucketCacheService;
        this.seckillStockBucketDomainService = seckillStockBucketDomainService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void arrangeStockBuckets(Long goodsId, Integer stock, Integer bucketQuantity, Integer assignmentMode) {
        LOGGER.info("arrangeStockBuckets|准备库存分桶|{}, {}, {}", goodsId, stock, bucketQuantity);
        ExceptionChecker.throwAssertIfNullOrEmpty(goodsId, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(stock, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfZeroOrNegative(stock, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(bucketQuantity, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfZeroOrNegative(bucketQuantity, RespCode.PARAMS_INVALID);

        DistributedLock distributedLock =
                distributedLockFactory.getDistributedLock(SeckillConstants.getKey(SeckillConstants.STOCK_BUCKET_SUSPEND_KEY, goodsId));

        try {
            boolean isLock = distributedLock.tryLock();
            if (!isLock) {
                LOGGER.info("arrangeStockBuckets|库存分桶时获取锁失败|{}", goodsId);
                return;
            }
            TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
            try {
                boolean suspendSuccess = seckillStockBucketDomainService.suspendBuckets(goodsId);
                if (!suspendSuccess) {
                    LOGGER.info("arrangeStockBuckets|关闭库存分桶失败|{}", goodsId);
                    throw new SeckillException(RespCode.BUCKET_CLOSED_FAILED);
                }
                dataSourceTransactionManager.commit(transactionStatus);
            } catch (Exception e) {
                LOGGER.info("arrangeStockBuckets|关闭分桶失败回滚中|{}", goodsId, e);
                dataSourceTransactionManager.rollback(transactionStatus);
            }
            List<SeckillStockBucket> buckets = seckillStockBucketDomainService.getBucketByGoodsId(goodsId);
            if (CollectionUtils.isEmpty(buckets)) {
                // 初始化库存分桶
                this.initStockBuckets(goodsId, stock, bucketQuantity);
                return;
            }
            if (SeckillStockBucketArrangementMode.isTotalArrangementMode(assignmentMode)) {
                this.arrangeStockBucketsBasedTotalMode(goodsId, stock, bucketQuantity, buckets);
            } else if (SeckillStockBucketArrangementMode.isIncrementalArrangementMode(assignmentMode)) {
                this.rearrangeStockBucketsBasedIncrementalMode(goodsId, stock, bucketQuantity, buckets);
            }
        } catch (Exception e) {
            LOGGER.error("arrangeStockBuckets|库存分桶错误|", e);
            throw new SeckillException(RespCode.BUCKET_CREATE_FAILED);
        } finally {
            distributedLock.unlock();
            // 打开分桶
            boolean success = seckillStockBucketDomainService.resumeBuckets(goodsId);
            if (!success) {
                LOGGER.error("arrangeStockBuckets|打开库存分桶失败|{}", goodsId);
            }
        }
    }

    @Override
    public SeckillStockBucketDTO getSeckillStockBucketDTO(Long goodsId, Long version) {
        ExceptionChecker.throwAssertIfNullOrEmpty(goodsId, RespCode.PARAMS_INVALID);

        SeckillBusinessCache<SeckillStockBucketDTO> seckillStockBucketCache =
                seckillStockBucketCacheService.getTotalStockBuckets(goodsId, version);

        // 稍后再试，前端需要对这个状态做特殊处理，即不要刷新数据，静默稍后再试
        ExceptionChecker.throwAssertIfTrue(seckillStockBucketCache.isRetryLater(), RespCode.RETRY_LATER);
        // 缓存中不存在商品库存数据
        ExceptionChecker.throwAssertIfFalse(seckillStockBucketCache.isExist(), RespCode.STOCK_IS_NULL);

        return seckillStockBucketCache.getData();
    }

    /**
     * 按照库存增量模式编排库存
     */
    private void rearrangeStockBucketsBasedIncrementalMode(Long goodsId,
                                                           Integer stock,
                                                           Integer bucketsQuantity,
                                                           List<SeckillStockBucket> buckets) {
        // 获取当前所有的可用库存
        int availableStock = buckets.stream().mapToInt(SeckillStockBucket::getAvailableStock).sum();
        // 获取当前所有的全部库存
        int allStock = buckets.stream().mapToInt(SeckillStockBucket::getInitialStock).sum();
        // 计算增加后的可用库存
        int totalAvailableStock = stock + availableStock;
        // 计算增加后的总库存
        int totalAllStock = stock + allStock;
        // 可用库存不足
        if (totalAvailableStock <= 0) {
            throw new SeckillException(RespCode.STOCK_LT_ZERO);
        }
        // 计算销量
        int soldStock = allStock - availableStock;
        // 销量大于总库存
        if (soldStock > totalAllStock) {
            throw new SeckillException(RespCode.BUCKET_SOLD_BEYOND_TOTAL);
        }
        // 提交分桶
        this.submitBuckets(goodsId, totalAllStock, totalAvailableStock, bucketsQuantity);
    }


    /**
     * 按照库存总量模式编排库存
     */
    private void arrangeStockBucketsBasedTotalMode(Long goodsId,
                                                   Integer stock,
                                                   Integer bucketsQuantity,
                                                   List<SeckillStockBucket> buckets) {
        // 获取当前所有的可用库存
        int availableStock = buckets.stream().mapToInt(SeckillStockBucket::getAvailableStock).sum();
        // 获取当前所有的库存
        int totalStock = buckets.stream().mapToInt(SeckillStockBucket::getInitialStock).sum();
        // 计算已售库存量
        int soldStock = totalStock - availableStock;
        // 已售商品储量大于传入的总商品量
        if (soldStock > stock) {
            throw new SeckillException(RespCode.BUCKET_SOLD_BEYOND_TOTAL);
        }
        // 计算当前可用库存量
        availableStock = availableStock + stock - totalStock;

        ExceptionChecker.throwAssertIfLessThan(availableStock, 0, RespCode.STOCK_LT_ZERO);

        // 提交分桶
        this.submitBuckets(goodsId, stock, availableStock, bucketsQuantity);
    }

    private void initStockBuckets(Long goodsId, Integer stock, Integer bucketsQuantity) {
        this.submitBuckets(goodsId, stock, stock, bucketsQuantity);
    }

    private void submitBuckets(Long goodsId, Integer initStock, Integer availableStock, Integer bucketsQuantity) {
        List<SeckillStockBucket> buckets = this.buildBuckets(goodsId, initStock, availableStock, bucketsQuantity);
        boolean success = seckillStockBucketDomainService.arrangeBuckets(goodsId, buckets);
        if (!success) {
            throw new SeckillException(RespCode.BUCKET_CREATE_FAILED);
        }

        // 保存每一项分桶库存到缓存
        buckets.forEach(bucket -> distributedCacheService.put(
                SeckillConstants.getKey(SeckillConstants.getKey(SeckillConstants.STOCK_BUCKET_AVAILABLE_KEY, goodsId),
                                        bucket.getSerialNo()), bucket.getAvailableStock()));

        // 保存分桶数量到缓存
        distributedCacheService.put(SeckillConstants.getKey(SeckillConstants.STOCK_BUCKET_QUANTITY_KEY, goodsId), buckets);
    }

    private List<SeckillStockBucket> buildBuckets(Long goodsId, Integer initStock, Integer availableStock, Integer bucketsQuantity) {
        ExceptionChecker.throwAssertIfNullOrEmpty(goodsId, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(initStock, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(availableStock, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(bucketsQuantity, RespCode.PARAMS_INVALID);

        List<SeckillStockBucket> buckets = new ArrayList<>();
        int initAverageStocks = initStock / bucketsQuantity;
        int initPieceStocks = initStock % bucketsQuantity;

        int availableAverageStocks = availableStock / bucketsQuantity;
        int availablePieceStocks = availableStock % bucketsQuantity;

        for (int i = 0; i < bucketsQuantity - 1; i++) {
            SeckillStockBucket seckillStockBucket = new SeckillStockBucket(goodsId, initAverageStocks, availableAverageStocks,
                                                                           SeckillStockBucketStatus.ENABLED.getCode(), i);
            buckets.add(seckillStockBucket);
        }
        int initRestStock = initAverageStocks + initPieceStocks;
        int availableRestStock = availableAverageStocks + availablePieceStocks;

        // 计算差值
        int subRestStock = availableRestStock - initRestStock;
        SeckillStockBucket seckillStockBucket =
                new SeckillStockBucket(goodsId, initRestStock, subRestStock > 0 ? initRestStock : availableRestStock,
                                       SeckillStockBucketStatus.ENABLED.getCode(), bucketsQuantity - 1);
        buckets.add(seckillStockBucket);
        return subRestStock > 0 ? this.buildBuckets(buckets, subRestStock) : buckets;
    }

    private List<SeckillStockBucket> buildBuckets(List<SeckillStockBucket> buckets, int subRestStock) {
        if (CollectionUtils.isEmpty(buckets)) {
            return buckets;
        }
        IntStream.range(0, subRestStock).forEach((i) -> {
            SeckillStockBucket bucket = buckets.get(i);
            bucket.setAvailableStock(bucket.getAvailableStock() + 1);
            buckets.set(i, bucket);
        });
        return buckets;
    }
}
