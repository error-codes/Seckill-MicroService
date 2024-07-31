package com.young.seckill.order.infrastructure.mapper;

import com.young.seckill.order.domain.entity.SeckillOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SeckillOrderMapper {

    /**
     * 保存订单
     */
    boolean saveSeckillOrder(SeckillOrder seckillOrder);

    /**
     * 根据用户ID获取订单列表
     */
    List<SeckillOrder> getSeckillOrderByUserId(@Param("userId") Long userId);

    /**
     * 根据活动ID获取订单列表
     */
    List<SeckillOrder> getSeckillOrderByActivityId(@Param("activityId") Long activityId);

    /**
     * 删除指定订单
     */
    boolean deleteSeckillOrder(@Param("orderId") Long orderId);
}
