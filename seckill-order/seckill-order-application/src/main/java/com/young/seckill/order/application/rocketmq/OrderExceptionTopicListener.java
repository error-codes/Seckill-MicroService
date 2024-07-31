package com.young.seckill.order.application.rocketmq;

import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.model.rocketmq.ExceptionTopicMessage;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.order.application.service.SeckillOrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RocketMQMessageListener(consumerGroup = SeckillConstants.TX_ORDER_CONSUMER_GROUP, topic = SeckillConstants.ERROR_MESSAGE_TOPIC)
public class OrderExceptionTopicListener implements RocketMQListener<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderExceptionTopicListener.class);

    private final SeckillOrderService seckillOrderService;

    public OrderExceptionTopicListener(SeckillOrderService seckillOrderService) {
        this.seckillOrderService = seckillOrderService;
    }

    @Override
    public void onMessage(String message) {
        LOGGER.info("onMessage|秒杀订单微服务开始消费消息: {}", message);

        if (!StringUtils.hasText(message)) {
            return;
        }
        // 删除数据库中对应订单
        seckillOrderService.deleteSeckillOrder(JACKSON.toObj(message, ExceptionTopicMessage.class));
    }
}
