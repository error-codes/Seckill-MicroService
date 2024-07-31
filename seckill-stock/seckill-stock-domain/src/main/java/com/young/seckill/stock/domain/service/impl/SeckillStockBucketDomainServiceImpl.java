package com.young.seckill.stock.domain.service.impl;

import com.young.seckill.MessageSenderService;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.exception.ExceptionChecker;
import com.young.seckill.common.response.RespCode;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.stock.domain.entity.SeckillStockBucket;
import com.young.seckill.stock.domain.entity.SeckillStockBucketDeduction;
import com.young.seckill.stock.domain.enums.SeckillStockBucketEventType;
import com.young.seckill.stock.domain.event.SeckillStockBucketEvent;
import com.young.seckill.stock.domain.repository.SeckillStockBucketRepository;
import com.young.seckill.stock.domain.service.SeckillStockBucketDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeckillStockBucketDomainServiceImpl implements SeckillStockBucketDomainService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillStockBucketDomainServiceImpl.class);
    private final SeckillStockBucketRepository seckillStockBucketRepository;
    private final MessageSenderService         messageSenderService;
    @Value("${message.send.type}")
    private       String                       eventType;

    public SeckillStockBucketDomainServiceImpl(SeckillStockBucketRepository seckillStockBucketRepository,
                                               MessageSenderService messageSenderService) {
        this.seckillStockBucketRepository = seckillStockBucketRepository;
        this.messageSenderService = messageSenderService;
    }

    @Override
    public boolean suspendBuckets(Long goodsId) {
        String eventName = "SUSPEND_BUCKETS";
        LOGGER.info("{}|禁用库存分桶|{}", eventName, goodsId);
        ExceptionChecker.throwAssertIfNullOrEmpty(goodsId, RespCode.PARAMS_INVALID);

        boolean success = seckillStockBucketRepository.suspendBuckets(goodsId);

        if (success) {
            SeckillStockBucketEvent seckillStockBucketEvent =
                    new SeckillStockBucketEvent(eventName, goodsId, SeckillStockBucketEventType.DISABLED.getCode(), getDestination());
            messageSenderService.sendMessage(seckillStockBucketEvent);
            LOGGER.info("{}|库存分桶已禁用|{}", eventName, goodsId);
            return true;
        }
        return false;
    }

    @Override
    public boolean resumeBuckets(Long goodsId) {
        String eventName = "RESUME_BUCKETS";
        LOGGER.info("{}|启用库存分桶|{}", eventName, goodsId);
        ExceptionChecker.throwAssertIfNullOrEmpty(goodsId, RespCode.PARAMS_INVALID);

        boolean success = seckillStockBucketRepository.resumeBuckets(goodsId);

        if (success) {
            SeckillStockBucketEvent seckillStockBucketEvent =
                    new SeckillStockBucketEvent(eventName, goodsId, SeckillStockBucketEventType.ENABLED.getCode(), getDestination());
            messageSenderService.sendMessage(seckillStockBucketEvent);
            LOGGER.info("{}|库存分桶已启用|{}", eventName, goodsId);
            return true;
        }
        return false;
    }

    @Override
    public List<SeckillStockBucket> getBucketByGoodsId(Long goodsId) {
        ExceptionChecker.throwAssertIfNullOrEmpty(goodsId, RespCode.PARAMS_INVALID);

        return seckillStockBucketRepository.getBucketByGoodsId(goodsId);
    }

    @Override
    public boolean arrangeBuckets(Long goodsId, List<SeckillStockBucket> buckets) {
        String eventName = "ARRANGE_BUCKETS";
        LOGGER.info("{}|编排库存分桶|{}", eventName, JACKSON.toJson(buckets));
        ExceptionChecker.throwAssertIfNullOrEmpty(goodsId, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(buckets, RespCode.PARAMS_INVALID);

        // 校验数据
        buckets.forEach((bucket) -> {
            ExceptionChecker.throwAssertIfNotEqual(goodsId, bucket.getGoodsId(), RespCode.BUCKET_GOODS_ID_ERROR);
            ExceptionChecker.throwAssertIfZeroOrNegative(bucket.getInitialStock(), RespCode.BUCKET_INIT_STOCK_ERROR);
            ExceptionChecker.throwAssertIfZeroOrNegative(bucket.getAvailableStock(), RespCode.BUCKET_AVAILABLE_STOCK_ERROR);
            ExceptionChecker.throwAssertIfLessThan(bucket.getInitialStock(), bucket.getAvailableStock(), RespCode.BUCKET_STOCK_ERROR);
        });

        // 存储分桶数据
        boolean success = seckillStockBucketRepository.submitBuckets(goodsId, buckets);

        if (success) {
            SeckillStockBucketEvent seckillStockBucketEvent =
                    new SeckillStockBucketEvent(eventName, goodsId, SeckillStockBucketEventType.ARRANGED.getCode(), getDestination());
            messageSenderService.sendMessage(seckillStockBucketEvent);
            LOGGER.info("{}|编排库存分桶已完成|{}", eventName, goodsId);
            return true;
        }
        return false;
    }

    @Override
    public boolean decreaseStockt(SeckillStockBucketDeduction seckillStockBucketDeduction) {
        String eventName = "DECREASE_STOCK";
        LOGGER.info("{}|扣减库存|{}", eventName, JACKSON.toJson(seckillStockBucketDeduction));
        ExceptionChecker.throwAssertIfNullOrEmpty(seckillStockBucketDeduction, RespCode.PARAMS_INVALID);

        return seckillStockBucketRepository.decreaseStockt(seckillStockBucketDeduction.getQuantity(),
                                                           seckillStockBucketDeduction.getSerialNo(),
                                                           seckillStockBucketDeduction.getGoodsId());
    }

    @Override
    public boolean increaseStock(SeckillStockBucketDeduction seckillStockBucketDeduction) {
        String eventName = "INCREASE_STOCK";
        LOGGER.info("{}|恢复库存|{}", eventName, JACKSON.toJson(seckillStockBucketDeduction));
        ExceptionChecker.throwAssertIfNullOrEmpty(seckillStockBucketDeduction, RespCode.PARAMS_INVALID);

        return seckillStockBucketRepository.increaseStock(seckillStockBucketDeduction.getQuantity(),
                                                          seckillStockBucketDeduction.getSerialNo(),
                                                          seckillStockBucketDeduction.getGoodsId());
    }

    private String getDestination() {
        return SeckillConstants.TYPE_ROCKETMQ.equals(eventType) ? SeckillConstants.EVENT_TOPIC_STOCK_KEY : SeckillConstants.TYPE_SPRING;
    }
}


