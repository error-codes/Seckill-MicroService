package com.young.seckill.order.application.place.impl;

import com.young.seckill.common.model.rocketmq.TransactionTopicMessage;
import com.young.seckill.order.application.model.command.SeckillOrderCommand;
import com.young.seckill.order.application.place.SeckillPlaceOrderService;
import org.springframework.stereotype.Service;

@Service
public class SeckillPlaceOrderBucketService implements SeckillPlaceOrderService {

    @Override
    public Long placeOrder(Long userId, SeckillOrderCommand seckillOrderCommand) {
        return 0;
    }

    @Override
    public void saveOrderInTransaction(TransactionTopicMessage transactionTopicMessage) {

    }
}
