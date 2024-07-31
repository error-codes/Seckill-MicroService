package com.young.seckill.goods.application.event;

import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.goods.application.cache.service.SeckillGoodsCacheService;
import com.young.seckill.goods.application.cache.service.SeckillGoodsListCacheService;
import com.young.seckill.goods.domain.event.SeckillGoodsEvent;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(name = "event.publish.type", havingValue = "rocketmq")
@RocketMQMessageListener(consumerGroup = SeckillConstants.EVENT_GOODS_CONSUMER_GROUP, topic = SeckillConstants.EVENT_TOPIC_GOODS_KEY)
public class RocketMQGoodsEventListener implements RocketMQListener<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQGoodsEventListener.class);

    private final SeckillGoodsCacheService     seckillGoodsCacheService;
    private final SeckillGoodsListCacheService seckillGoodsListCacheService;

    public RocketMQGoodsEventListener(SeckillGoodsCacheService seckillGoodsCacheService,
                                      SeckillGoodsListCacheService seckillGoodsListCacheService) {
        this.seckillGoodsCacheService = seckillGoodsCacheService;
        this.seckillGoodsListCacheService = seckillGoodsListCacheService;
    }

    @Override
    public void onMessage(String message) {
        LOGGER.info("RocketMQ|GoodsEvent 接收商品事件|{}", message);
        if (!StringUtils.hasText(message)) {
            LOGGER.info("RocketMQ|GoodsEvent|事件参数错误");
            return;
        }
        SeckillGoodsEvent seckillGoodsEvent = getEventMessage(message);

        seckillGoodsCacheService.tryUpdateSeckillGoodsCacheByLock(seckillGoodsEvent.getGoodsId(), false);
        seckillGoodsListCacheService.tryUpdateSeckillGoodsCacheByLock(seckillGoodsEvent.getActivityId(), false);
    }

    private SeckillGoodsEvent getEventMessage(String message) {
        return JACKSON.toObj(message, SeckillGoodsEvent.class);
    }
}
