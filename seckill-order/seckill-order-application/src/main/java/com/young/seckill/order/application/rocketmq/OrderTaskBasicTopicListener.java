package com.young.seckill.order.application.rocketmq;

import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.order.application.model.task.SeckillOrderTask;
import com.young.seckill.order.application.service.SeckillSubmitOrderService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(name = "submit.order.type", havingValue = "async")
@RocketMQMessageListener(consumerGroup = SeckillConstants.SUBMIT_ORDER_CONSUMER_GROUP,
                         topic = SeckillConstants.SUBMIT_ORDER_MESSAGE_TOPIC)
public class OrderTaskBasicTopicListener implements RocketMQListener<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderTaskBasicTopicListener.class);

    private final SeckillSubmitOrderService seckillSubmitOrderService;

    public OrderTaskBasicTopicListener(@Qualifier("seckillAsyncSubmitOrderServiceImpl") SeckillSubmitOrderService seckillSubmitOrderService) {
        this.seckillSubmitOrderService = seckillSubmitOrderService;
    }

    @Override
    public void onMessage(String message) {
        LOGGER.info("onMessage|秒杀订单微服务接收异步订单任务消息: {}", message);
        if (!StringUtils.hasText(message)) {
            LOGGER.info("onMessage|秒杀订单微服务接收异步订单任务消息为空: {}", message);
            return;
        }
        SeckillOrderTask seckillOrderTask = JACKSON.toObj(message, SeckillOrderTask.class);
        if (seckillOrderTask == null) {
            LOGGER.info("onMessage|秒杀订单微服务接收异步订单任务消息转换成任务对象为空: {}", message);
            return;
        }
        LOGGER.info("onMessage|处理下单任务: {}", seckillOrderTask.getOrderTaskId());
        seckillSubmitOrderService.handlePlaceOrderTask(seckillOrderTask);

    }

}
