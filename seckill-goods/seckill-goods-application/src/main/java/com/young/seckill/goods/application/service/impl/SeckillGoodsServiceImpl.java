package com.young.seckill.goods.application.service.impl;

import com.young.seckill.common.cache.distribute.DistributedCacheService;
import com.young.seckill.common.cache.local.LocalCacheService;
import com.young.seckill.common.cache.model.SeckillBusinessCache;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.exception.ExceptionChecker;
import com.young.seckill.common.model.dto.SeckillActivityDTO;
import com.young.seckill.common.model.dto.SeckillGoodsDTO;
import com.young.seckill.common.model.enums.SeckillGoodsStatus;
import com.young.seckill.common.model.rocketmq.ExceptionTopicMessage;
import com.young.seckill.common.model.rocketmq.TransactionTopicMessage;
import com.young.seckill.common.response.RespCode;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.common.utils.SnowFlakeFactory;
import com.young.seckill.dubbo.interfaces.activity.SeckillActivityDubboService;
import com.young.seckill.goods.application.cache.service.SeckillGoodsCacheService;
import com.young.seckill.goods.application.cache.service.SeckillGoodsListCacheService;
import com.young.seckill.goods.application.command.SeckillGoodsCommand;
import com.young.seckill.goods.application.service.SeckillGoodsService;
import com.young.seckill.goods.domain.entity.SeckillGoods;
import com.young.seckill.goods.domain.service.SeckillGoodsDomainService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    private static final Logger                                  LOGGER = LoggerFactory.getLogger(SeckillGoodsServiceImpl.class);
    private final        LocalCacheService<String, SeckillGoods> localCacheService;
    private final        DistributedCacheService                 distributedCacheService;
    private final        SeckillGoodsCacheService                seckillGoodsCacheService;
    private final        SeckillGoodsDomainService               seckillGoodsDomainService;
    private final        SeckillGoodsListCacheService            seckillGoodsListCacheService;
    private final        RocketMQTemplate                        rocketMQTemplate;
    @DubboReference(version = "1.0.0")
    private              SeckillActivityDubboService             seckillActivityDubboService;

    public SeckillGoodsServiceImpl(LocalCacheService<String, SeckillGoods> localCacheService,
                                   DistributedCacheService distributedCacheService,
                                   SeckillGoodsCacheService seckillGoodsCacheService,
                                   SeckillGoodsDomainService seckillGoodsDomainService,
                                   SeckillGoodsListCacheService seckillGoodsListCacheService,
                                   RocketMQTemplate rocketMQTemplate) {
        this.localCacheService = localCacheService;
        this.distributedCacheService = distributedCacheService;
        this.seckillGoodsCacheService = seckillGoodsCacheService;
        this.seckillGoodsDomainService = seckillGoodsDomainService;
        this.seckillGoodsListCacheService = seckillGoodsListCacheService;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public void saveSeckillGoods(SeckillGoodsCommand seckillGoodsCommand) {
        ExceptionChecker.throwAssertIfNullOrEmpty(seckillGoodsCommand, RespCode.PARAMS_INVALID);

        SeckillActivityDTO seckillActivity =
                seckillActivityDubboService.getSeckillActivity(seckillGoodsCommand.getActivityId(), seckillGoodsCommand.getVersion());

        ExceptionChecker.throwAssertIfNullOrEmpty(seckillActivity, RespCode.ACTIVITY_NOT_EXISTS);

        SeckillGoods seckillGoods = new SeckillGoods();
        BeanUtils.copyProperties(seckillGoodsCommand, seckillGoods);
        seckillGoods.setStartTime(seckillActivity.getStartTime());
        seckillGoods.setEndTime(seckillActivity.getEndTime());
        seckillGoods.setAvailableStock(seckillGoodsCommand.getInitialStock());
        seckillGoods.setId(SnowFlakeFactory.getSnowFlakeIDCache().nextId());
        seckillGoods.setStatus(SeckillGoodsStatus.PUBLISHED.getCode());

        // 将商品库存同步到分布式缓存中
        distributedCacheService.put(SeckillConstants.getKey(SeckillConstants.GOODS_ITEM_STOCK_KEY_PREFIX, seckillGoods.getId()),
                                    seckillGoods.getAvailableStock());
        // 将商品限购同步到分布式缓存中
        distributedCacheService.put(SeckillConstants.getKey(SeckillConstants.GOODS_ITEM_LIMIT_KEY_PREFIX, seckillGoods.getId()),
                                    seckillGoods.getLimitNum());
        seckillGoodsDomainService.saveSeckillGoods(seckillGoods);

    }

    @Override
    public SeckillGoods getSeckillGoodsById(Long goodsId) {
        String cacheKey = SeckillConstants.getKey(SeckillConstants.GOODS_KEY, goodsId);
        // 从本地缓存尝试获取
        SeckillGoods seckillGoods = localCacheService.getIfPresent(cacheKey);
        // 如果本地缓存为空，则从分布式缓存尝试获取
        if (seckillGoods == null) {
            seckillGoods = distributedCacheService.getObject(cacheKey, SeckillGoods.class);
            // 如果分布式缓存为空，则从数据库获取
            if (seckillGoods == null) {
                seckillGoods = seckillGoodsDomainService.getSeckillGoodsById(goodsId);
                // 如果数据库获取到数据，将其缓存到分布式缓存
                if (seckillGoods != null) {
                    distributedCacheService.put(cacheKey, seckillGoods, 10, TimeUnit.MINUTES);
                }
            }

            // 如果分布式缓存获取数据库获取到数据，将其缓存到本地缓存
            if (seckillGoods != null) {
                localCacheService.put(cacheKey, seckillGoods);
            }
        }
        return seckillGoods;
    }

    @Override
    public SeckillGoodsDTO getSeckillGoodsById(Long goodsId, Long version) {
        ExceptionChecker.throwAssertIfNullOrEmpty(goodsId, RespCode.PARAMS_INVALID);

        SeckillBusinessCache<SeckillGoods> seckillGoodsCache = seckillGoodsCacheService.getCacheGoods(goodsId, version);

        ExceptionChecker.throwAssertIfFalse(seckillGoodsCache.isExist(), RespCode.ACTIVITY_NOT_EXISTS);
        ExceptionChecker.throwAssertIfTrue(seckillGoodsCache.isRetryLater(), RespCode.RETRY_LATER);

        SeckillGoodsDTO seckillGoodsDTO = new SeckillGoodsDTO();
        BeanUtils.copyProperties(seckillGoodsCache.getData(), seckillGoodsDTO);
        seckillGoodsDTO.setVersion(seckillGoodsCache.getVersion());
        return seckillGoodsDTO;
    }

    @Override
    public List<SeckillGoods> getSeckillGoodsByActivityId(Long activityId) {
        return seckillGoodsDomainService.getSeckillGoodsByActivityId(activityId);
    }

    @Override
    public List<SeckillGoodsDTO> getSeckillGoodsByActivityId(Long activityId, Long version) {
        ExceptionChecker.throwAssertIfNullOrEmpty(activityId, RespCode.PARAMS_INVALID);

        SeckillBusinessCache<List<SeckillGoods>> seckillGoodsListCache =
                seckillGoodsListCacheService.getCacheGoods(activityId, version);

        ExceptionChecker.throwAssertIfFalse(seckillGoodsListCache.isExist(), RespCode.ACTIVITY_NOT_EXISTS);
        ExceptionChecker.throwAssertIfTrue(seckillGoodsListCache.isRetryLater(), RespCode.RETRY_LATER);

        return seckillGoodsListCache.getData().stream().map(seckillGoods -> {
            SeckillGoodsDTO seckillGoodsDTO = new SeckillGoodsDTO();
            BeanUtils.copyProperties(seckillGoods, seckillGoodsDTO);
            seckillGoodsDTO.setVersion(seckillGoodsListCache.getVersion());
            return seckillGoodsDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public void updateGoodsStatus(Long goodsId, Integer status) {
        if (Objects.equals(status, SeckillGoodsStatus.OFFLINE.getCode())) {
            // 清除商品限购的分布式缓存
            distributedCacheService.delete(SeckillConstants.getKey(SeckillConstants.GOODS_ITEM_LIMIT_KEY_PREFIX, goodsId));
            // 清除商品库存的分布式缓存
            distributedCacheService.delete(SeckillConstants.getKey(SeckillConstants.GOODS_ITEM_STOCK_KEY_PREFIX, goodsId));
            // 清除商品详情的本地缓存
            localCacheService.delete(SeckillConstants.getKey(SeckillConstants.GOODS_KEY, goodsId));
            // 清除商品详情的分布式缓存
            distributedCacheService.delete(SeckillConstants.getKey(SeckillConstants.GOODS_KEY, goodsId));
        }
        seckillGoodsDomainService.updateGoodsStatus(goodsId, status);
    }

    @Override
    public boolean incrementGoodsAvailableStock(Long goodsId, Integer stock) {
        return seckillGoodsDomainService.incrementAvailableStock(goodsId, stock);
    }

    @Override
    public boolean updateGoodsAvailableStock(Long goodsId, Integer quantity) {
        return seckillGoodsDomainService.updateGoodsAvailableStock(goodsId, quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateGoodsAvailableStock(TransactionTopicMessage transactionTopicMessage) {
        Boolean decrementStock = distributedCacheService.hasKey(
                SeckillConstants.getKey(SeckillConstants.GOODS_TX_KEY, transactionTopicMessage.getTxNo()));
        if (decrementStock) {
            LOGGER.info("updateAvailableStock|秒杀商品微服务已经扣减过库存|{}", transactionTopicMessage.getTxNo());
            return true;
        }
        boolean updated = false;

        try {
            updated = seckillGoodsDomainService.updateGoodsAvailableStock(transactionTopicMessage.getGoodsId(),
                                                                          transactionTopicMessage.getQuantity());
            // 成功扣减库存
            if (updated) {
                distributedCacheService.put(SeckillConstants.getKey(SeckillConstants.GOODS_TX_KEY, transactionTopicMessage.getTxNo()),
                                            transactionTopicMessage.getTxNo(), SeckillConstants.TX_LOG_VALID_DURATION, TimeUnit.DAYS);
            } else {
                // 发送失败消息给订单微服务
                rocketMQTemplate.send(SeckillConstants.ERROR_MESSAGE_TOPIC, getErrorMessage(transactionTopicMessage));
            }
        } catch (Exception e) {
            updated = false;
            LOGGER.error("updateAvailableStock|抛出异常|参数: {}|异常: {}", transactionTopicMessage.getTxNo(), e.getMessage());
            // 发送失败消息给订单服务
            rocketMQTemplate.send(SeckillConstants.ERROR_MESSAGE_TOPIC, getErrorMessage(transactionTopicMessage));
        }
        return updated;
    }

    @Override
    public Integer getAvailableStockByGoodsId(Long goodsId) {
        return seckillGoodsDomainService.getAvailableStockById(goodsId);
    }

    private Message<String> getErrorMessage(TransactionTopicMessage transactionTopicMessage) {
        ExceptionTopicMessage exceptionTopicMessage =
                new ExceptionTopicMessage(SeckillConstants.ERROR_MESSAGE_TOPIC, transactionTopicMessage.getTxNo(),
                                          transactionTopicMessage.getGoodsId(), transactionTopicMessage.getQuantity(),
                                          transactionTopicMessage.getOrderPlaceType(), transactionTopicMessage.getException());

        return MessageBuilder.withPayload(Objects.requireNonNull(JACKSON.toJson(exceptionTopicMessage))).build();
    }
}
