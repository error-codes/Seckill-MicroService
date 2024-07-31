package com.young.seckill.order.application.service;

import com.young.seckill.common.model.rocketmq.ExceptionTopicMessage;
import com.young.seckill.order.application.model.command.SeckillOrderCommand;
import com.young.seckill.order.domain.entity.SeckillOrder;

import java.util.List;

public interface SeckillOrderService {

    /**
     * 创建订单
     *
     * @param userId              用户ID
     * @param seckillOrderCommand 订单信息
     */
    Long saveSeckillOrder(Long userId, SeckillOrderCommand seckillOrderCommand);

    /**
     * 根据用户ID获取订单列表
     *
     * @param userId 用户ID
     */
    List<SeckillOrder> getSeckillOrderByUserId(Long userId);

    /**
     * 根据活动ID获取订单列表
     *
     * @param activityId 活动ID
     */
    List<SeckillOrder> getSeckillOrderByActivityId(Long activityId);

    /**
     * 根据错误消息删除订单
     *
     * @param exceptionTopicMessage 错误消息
     */
    void deleteSeckillOrder(ExceptionTopicMessage exceptionTopicMessage);
}
