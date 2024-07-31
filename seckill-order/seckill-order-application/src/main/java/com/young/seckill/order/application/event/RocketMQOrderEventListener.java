package com.young.seckill.order.application.event;

import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.order.domain.event.SeckillOrderEvent;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(name = "event.publish.type", havingValue = "rocketmq")
@RocketMQMessageListener(consumerGroup = SeckillConstants.EVENT_ORDER_CONSUMER_GROUP, topic = SeckillConstants.EVENT_TOPIC_ORDER_KEY)
public class RocketMQOrderEventListener implements RocketMQListener<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQOrderEventListener.class);

    @Override
    public void onMessage(String message) {
        LOGGER.info("RocketMQ|OrderEvent 接收活动事件|{}", message);
        if (!StringUtils.hasText(message)) {
            LOGGER.info("RocketMQ|OrderEvent|事件参数错误");
            return;
        }
        SeckillOrderEvent seckillOrderEvent = getEventMessage(message);
        if (seckillOrderEvent.getOrderId() == null) {
            LOGGER.info("RocketMQ|OrderEvent|订单参数错误");
        }
    }

    private SeckillOrderEvent getEventMessage(String message) {
        return JACKSON.toObj(message, SeckillOrderEvent.class);
    }
}
