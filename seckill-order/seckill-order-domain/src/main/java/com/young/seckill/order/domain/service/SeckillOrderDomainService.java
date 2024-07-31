package com.young.seckill.order.domain.service;

import com.young.seckill.order.domain.entity.SeckillOrder;

import java.util.List;

public interface SeckillOrderDomainService {

    /**
     * 创建订单
     *
     * @param seckillOrder 订单信息
     */
    boolean saveSeckillOrder(SeckillOrder seckillOrder);

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
     * 删除指定订单
     *
     * @param orderId 订单ID
     */
    void deleteSeckillOrder(Long orderId);
}