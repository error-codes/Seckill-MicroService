package com.young.seckill.order.application.service;

import com.young.seckill.common.model.dto.SeckillSubmitOrderDTO;
import com.young.seckill.order.application.model.command.SeckillOrderCommand;
import com.young.seckill.order.application.model.task.SeckillOrderTask;

public interface SeckillSubmitOrderService {

    /**
     * 保存订单
     *
     * @param userId              用户ID
     * @param seckillOrderCommand 订单信息
     */
    SeckillSubmitOrderDTO saveSeckillOrder(Long userId, SeckillOrderCommand seckillOrderCommand);

    /**
     * 处理订单任务
     *
     * @param seckillOrderTask 订单任务
     */
    default void handlePlaceOrderTask(SeckillOrderTask seckillOrderTask) {
    }

    /**
     * 检查订单参数
     *
     * @param userId              用户ID
     * @param seckillOrderCommand 订单信息
     */
    default void checkSeckillOrder(Long userId, SeckillOrderCommand seckillOrderCommand) {
    }
}
