package com.young.seckill.order.application.rocketmq;

import com.young.seckill.common.cache.distribute.DistributedCacheService;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.model.rocketmq.TransactionTopicMessage;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.order.application.place.SeckillPlaceOrderService;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@RocketMQTransactionListener(rocketMQTemplateBeanName = "rocketMQTemplate")
public class OrderTransactionTopicListener implements RocketMQLocalTransactionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderTransactionTopicListener.class);

    private final SeckillPlaceOrderService seckillPlaceOrderService;
    private final DistributedCacheService  distributedCacheService;

    public OrderTransactionTopicListener(SeckillPlaceOrderService seckillPlaceOrderService,
                                         DistributedCacheService distributedCacheService) {
        this.seckillPlaceOrderService = seckillPlaceOrderService;
        this.distributedCacheService = distributedCacheService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        TransactionTopicMessage transactionTopicMessage =
                JACKSON.toObj(new String((byte[]) message.getPayload()), TransactionTopicMessage.class);
        try {
            // 已经抛出了异常，则直接回滚
            if (transactionTopicMessage.getException()) {
                return RocketMQLocalTransactionState.ROLLBACK;
            }
            seckillPlaceOrderService.saveOrderInTransaction(transactionTopicMessage);
            LOGGER.info("executeLocalTransaction|秒杀订单微服务成功提交本地事务|{}", transactionTopicMessage.getTxNo());
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            LOGGER.error("executeLocalTransaction|秒杀订单微服务异常回滚事务|{}", transactionTopicMessage.getTxNo());
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        TransactionTopicMessage transactionTopicMessage = JACKSON.toObj((String) message.getPayload(), TransactionTopicMessage.class);
        LOGGER.info("checkLocalTransaction|秒杀订单微服务查询本地事务|{}", transactionTopicMessage.getTxNo());
        Boolean submit = distributedCacheService.hasKey(
                SeckillConstants.getKey(SeckillConstants.ORDER_TX_KEY, transactionTopicMessage.getTxNo()));
        return submit ? RocketMQLocalTransactionState.COMMIT : RocketMQLocalTransactionState.UNKNOWN;
    }
}
