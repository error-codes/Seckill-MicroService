package com.young.seckill.stock.application.service.impl;

import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.exception.ExceptionChecker;
import com.young.seckill.common.exception.SeckillException;
import com.young.seckill.common.lock.DistributedLock;
import com.young.seckill.common.lock.factory.DistributedLockFactory;
import com.young.seckill.common.response.RespCode;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.stock.application.model.SeckillStockBucketDTO;
import com.young.seckill.stock.application.model.SeckillStockBucketWrapperCommand;
import com.young.seckill.stock.application.service.SeckillStockBucketArrangementService;
import com.young.seckill.stock.application.service.SeckillStockBucketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillStockBucketServiceImpl implements SeckillStockBucketService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillStockBucketServiceImpl.class);

    private final DistributedLockFactory               distributedLockFactory;
    private final SeckillStockBucketArrangementService seckillStockBucketArrangementService;

    @Autowired
    public SeckillStockBucketServiceImpl(DistributedLockFactory distributedLockFactory,
                                         SeckillStockBucketArrangementService seckillStockBucketArrangementService) {
        this.distributedLockFactory = distributedLockFactory;
        this.seckillStockBucketArrangementService = seckillStockBucketArrangementService;
    }

    @Override
    public void arrangeStockBuckets(Long userId, SeckillStockBucketWrapperCommand stockBucketWrapperCommand) {
        ExceptionChecker.throwAssertIfNullOrEmpty(userId, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(stockBucketWrapperCommand, RespCode.PARAMS_INVALID);

        stockBucketWrapperCommand.setUserId(userId);
        ExceptionChecker.throwAssertIfTrue(stockBucketWrapperCommand.isEmpty(), RespCode.PARAMS_INVALID);
        LOGGER.info("arrangeStockBuckets|编排库存分桶|{}", JACKSON.toJson(stockBucketWrapperCommand));
        String localKey = SeckillConstants.getKey(
                SeckillConstants.getKey(SeckillConstants.STOCK_BUCKET_ARRANGEMENT_KEY, stockBucketWrapperCommand.getUserId()),
                stockBucketWrapperCommand.getGoodsId());

        DistributedLock distributedLock = distributedLockFactory.getDistributedLock(localKey);
        try {
            boolean isLock = distributedLock.tryLock();
            ExceptionChecker.throwAssertIfFalse(isLock, RespCode.FREQUENTLY_ERROR);
            // 获取到锁，编排库存
            seckillStockBucketArrangementService.arrangeStockBuckets(stockBucketWrapperCommand.getGoodsId(),
                                                                     stockBucketWrapperCommand.getSeckillStockBucketCommand()
                                                                                              .getTotalStock(),
                                                                     stockBucketWrapperCommand.getSeckillStockBucketCommand()
                                                                                              .getBucketsQuantity(),
                                                                     stockBucketWrapperCommand.getSeckillStockBucketCommand()
                                                                                              .getArrangementMode());

            LOGGER.info("arrangeStockBuckets|库存编排完成|{}", stockBucketWrapperCommand.getGoodsId());
        } catch (SeckillException e) {
            LOGGER.error("arrangeStockBuckets|库存编排失败|{}", stockBucketWrapperCommand.getGoodsId(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("arrangeStockBuckets|库存编排错误|{}", stockBucketWrapperCommand.getGoodsId(), e);
            throw new SeckillException(RespCode.BUCKET_CREATE_FAILED);
        } finally {
            distributedLock.unlock();
        }
    }


    @Override
    public SeckillStockBucketDTO getTotalStockBuckets(Long goodsId, Long version) {
        ExceptionChecker.throwAssertIfNullOrEmpty(goodsId, RespCode.PARAMS_INVALID);
        LOGGER.info("stockBucketsSummary|获取库存分桶数据|{}", goodsId);
        return seckillStockBucketArrangementService.getSeckillStockBucketDTO(goodsId, version);
    }
}
