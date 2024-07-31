package com.young.seckill.stock.application.event;

import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.stock.application.cache.SeckillStockBucketCacheService;
import com.young.seckill.stock.domain.enums.SeckillStockBucketEventType;
import com.young.seckill.stock.domain.event.SeckillStockBucketEvent;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(value = "event.publish.type", havingValue = "rocketmq")
@RocketMQMessageListener(consumerGroup = SeckillConstants.EVENT_STOCK_CONSUMER_GROUP, topic = SeckillConstants.EVENT_TOPIC_STOCK_KEY)
public class RocketMQStockEventListener implements RocketMQListener<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQStockEventListener.class);

    private final SeckillStockBucketCacheService seckillStockBucketCacheService;

    public RocketMQStockEventListener(SeckillStockBucketCacheService seckillStockBucketCacheService) {
        this.seckillStockBucketCacheService = seckillStockBucketCacheService;
    }

    @Override
    public void onMessage(String message) {
        if (!StringUtils.hasText(message)) {
            LOGGER.info("RocketMQ|StockEvent 接收库存事件为空");
            return;
        }
        SeckillStockBucketEvent event = getEventMessage(message);
        if (event == null || event.getGoodsId() == null) {
            LOGGER.info("RocketMQ|StockEvent|库存参数错误");
        } else {
            LOGGER.info("RocketMQ|StockEvent|接受库存事件");
            // 开启了库存分桶，更新缓存数据
            if (SeckillStockBucketEventType.ENABLED.getCode().equals(event.getStatus())) {
                seckillStockBucketCacheService.tryUpdateSeckillStockBucketCacheByLock(event.getGoodsId(), false);
            }
        }
    }

    private SeckillStockBucketEvent getEventMessage(String message) {
        return JACKSON.toObj(message, SeckillStockBucketEvent.class);
    }
}
