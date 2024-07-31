package com.young.seckill.order.application.service;

import com.young.seckill.order.application.model.task.SeckillOrderTask;

public interface PlaceOrderTaskService {

    /**
     * 提交下单任务
     *
     * @param seckillOrderTask 订单任务
     */
    boolean submitOrderTask(SeckillOrderTask seckillOrderTask);
}
