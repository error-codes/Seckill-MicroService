package com.young.seckill.goods.application.rocketmq;

import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.model.rocketmq.TransactionTopicMessage;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.goods.application.service.SeckillGoodsService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RocketMQMessageListener(consumerGroup = SeckillConstants.TX_GOODS_CONSUMER_GROUP, topic = SeckillConstants.TX_MESSAGE_TOPIC)
public class GoodsTransactionTopicListener implements RocketMQListener<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsTransactionTopicListener.class);

    private final SeckillGoodsService seckillGoodsService;

    public GoodsTransactionTopicListener(SeckillGoodsService seckillGoodsService) {
        this.seckillGoodsService = seckillGoodsService;
    }

    @Override
    public void onMessage(String message) {
        if (!StringUtils.hasText(message)) {
            return;
        }
        LOGGER.info("秒杀商品微服务开始消费事务消息：{}", message);
        TransactionTopicMessage transactionTopicMessage = JACKSON.toObj(message, TransactionTopicMessage.class);
        // 如果协调的异常信息字段为 false，订单微服务没有抛出异常，则处理库存信息
        if (!transactionTopicMessage.getException()) {
            seckillGoodsService.updateGoodsAvailableStock(transactionTopicMessage);
        }
    }
}
