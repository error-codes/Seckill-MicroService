package com.young.seckill.goods.domain.service.impl;

import com.young.seckill.MessageSenderService;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.exception.ExceptionChecker;
import com.young.seckill.common.model.enums.SeckillGoodsStatus;
import com.young.seckill.common.response.RespCode;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.goods.domain.entity.SeckillGoods;
import com.young.seckill.goods.domain.event.SeckillGoodsEvent;
import com.young.seckill.goods.domain.repository.SeckillGoodsRepository;
import com.young.seckill.goods.domain.service.SeckillGoodsDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeckillGoodsDomainServiceImpl implements SeckillGoodsDomainService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillGoodsDomainServiceImpl.class);

    private final SeckillGoodsRepository seckillGoodsRepository;
    private final MessageSenderService   messageSenderService;

    public SeckillGoodsDomainServiceImpl(SeckillGoodsRepository seckillGoodsRepository, MessageSenderService messageSenderService) {
        this.seckillGoodsRepository = seckillGoodsRepository;
        this.messageSenderService = messageSenderService;
    }

    @Override
    public void saveSeckillGoods(SeckillGoods seckillGoods) {
        String eventName = "GOODS_PUBLISH";
        LOGGER.info("{}|发布秒杀商品|{}", eventName, JACKSON.toJson(seckillGoods));

        ExceptionChecker.throwAssertIfNullOrEmpty(seckillGoods, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfBlank(seckillGoods.getGoodsName(), RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(seckillGoods.getActivityId(), RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(seckillGoods.getStartTime(), RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(seckillGoods.getEndTime(), RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfFalse(seckillGoods.getStartTime().isBefore(seckillGoods.getEndTime()), RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(seckillGoods.getActivityPrice(), RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfZeroOrNegative(seckillGoods.getActivityPrice(), RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(seckillGoods.getOriginalPrice(), RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfZeroOrNegative(seckillGoods.getOriginalPrice(), RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(seckillGoods.getInitialStock(), RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfZeroOrNegative(seckillGoods.getInitialStock(), RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(seckillGoods.getLimitNum(), RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfZeroOrNegative(seckillGoods.getLimitNum(), RespCode.PARAMS_INVALID);

        seckillGoods.setStatus(SeckillGoodsStatus.PUBLISHED.getCode());
        seckillGoodsRepository.saveSeckillGoods(seckillGoods);

        LOGGER.info("{}|秒杀商品已经发布|{}", eventName, seckillGoods.getId());

        SeckillGoodsEvent seckillGoodsEvent =
                new SeckillGoodsEvent(eventName, seckillGoods.getId(), seckillGoods.getActivityId(), seckillGoods.getStatus(),
                                      SeckillConstants.EVENT_TOPIC_GOODS_KEY);
        messageSenderService.sendMessage(seckillGoodsEvent);
        LOGGER.info("{}|秒杀商品事件已发布|{}", eventName, seckillGoodsEvent);
    }

    @Override
    public SeckillGoods getSeckillGoodsById(Long goodsId) {
        return seckillGoodsRepository.getSeckillGoodsById(goodsId);
    }

    @Override
    public List<SeckillGoods> getSeckillGoodsByActivityId(Long activityId) {
        return seckillGoodsRepository.getSeckillGoodsByActivityId(activityId);
    }

    @Override
    public void updateGoodsStatus(Long goodsId, Integer status) {
        String eventName = "GOODS_UPDATE";
        LOGGER.info("{}|更新秒杀商品状态|{}", eventName, goodsId);

        ExceptionChecker.throwAssertIfNullOrEmpty(goodsId, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(status, RespCode.PARAMS_INVALID);

        SeckillGoods seckillGoods = seckillGoodsRepository.getSeckillGoodsById(goodsId);

        ExceptionChecker.throwAssertIfNullOrEmpty(seckillGoods, RespCode.GOODS_NOT_EXISTS);

        Integer result = seckillGoodsRepository.updateGoodsStatus(goodsId, status);
        LOGGER.info("{}|秒杀商品状态已修改|{}", eventName, goodsId);

        SeckillGoodsEvent seckillGoodsEvent = new SeckillGoodsEvent(eventName, goodsId, seckillGoods.getActivityId(), status,
                                                                    SeckillConstants.EVENT_TOPIC_GOODS_KEY);
        messageSenderService.sendMessage(seckillGoodsEvent);
        LOGGER.info("{}|秒杀商品事件已发布|{}", eventName, JACKSON.toJson(seckillGoodsEvent));
    }

    @Override
    public boolean updateGoodsAvailableStock(Long goodsId, Integer quantity) {
        String eventName = "GOODS_UPDATE";
        LOGGER.info("{}|修改秒杀商品库存|{}", eventName, goodsId);

        ExceptionChecker.throwAssertIfNullOrEmpty(goodsId, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(quantity, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfZeroOrNegative(quantity, RespCode.PARAMS_INVALID);

        SeckillGoods seckillGoods = seckillGoodsRepository.getSeckillGoodsById(goodsId);

        ExceptionChecker.throwAssertIfNullOrEmpty(seckillGoods, RespCode.GOODS_NOT_EXISTS);

        boolean updated = seckillGoodsRepository.updateGoodsAvailableStock(goodsId, quantity) > 0;
        if (updated) {
            LOGGER.info("{}|秒杀商品库存已修改|{}", eventName, goodsId);
            SeckillGoodsEvent seckillGoodsEvent = new SeckillGoodsEvent(eventName, goodsId, seckillGoods.getActivityId(), quantity,
                                                                        SeckillConstants.EVENT_TOPIC_GOODS_KEY);
            messageSenderService.sendMessage(seckillGoodsEvent);
            LOGGER.info("{}|秒杀商品库存事件已发布|{}", eventName, JACKSON.toJson(seckillGoodsEvent));
        } else {
            LOGGER.info("{}|秒杀商品库存未修改|{}", eventName, goodsId);
        }
        return updated;
    }

    @Override
    public boolean incrementAvailableStock(Long goodsId, Integer stock) {
        String eventName = "GOODS_INCREMENT";
        LOGGER.info("{}|增加秒杀商品库存|{}", eventName, goodsId);
        ExceptionChecker.throwAssertIfNullOrEmpty(stock, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(goodsId, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfZeroOrNegative(stock, RespCode.PARAMS_INVALID);

        SeckillGoods seckillGoods = seckillGoodsRepository.getSeckillGoodsById(goodsId);

        ExceptionChecker.throwAssertIfNullOrEmpty(seckillGoods, RespCode.GOODS_NOT_EXISTS);

        boolean updated = seckillGoodsRepository.incrementAvailableStock(goodsId, stock) > 0;
        if (updated) {
            LOGGER.info("{}|秒杀商品库存已经更新|{}", eventName, goodsId);
            SeckillGoodsEvent seckillGoodsEvent =
                    new SeckillGoodsEvent(eventName, seckillGoods.getId(), seckillGoods.getActivityId(), seckillGoods.getStatus(),
                                          SeckillConstants.EVENT_TOPIC_GOODS_KEY);
            messageSenderService.sendMessage(seckillGoodsEvent);
            LOGGER.info("{}|增加秒杀商品库存事件已经发布|{}", eventName, goodsId);
        } else {
            LOGGER.info("{}|秒杀商品库存未增加|{}", eventName, goodsId);
        }
        return updated;
    }

    @Override
    public Integer getAvailableStockById(Long goodsId) {
        return seckillGoodsRepository.getAvailableStockById(goodsId);
    }
}
