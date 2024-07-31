package com.young.seckill.activity.infrastructure.mapper;

import com.young.seckill.activity.domain.entity.SeckillActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SeckillActivityMapper {

    /**
     * 保存秒杀活动
     */
    void saveSeckillActivity(SeckillActivity seckillActivity);

    /**
     * 根据状态获取秒杀活动列表
     */
    List<SeckillActivity> getSeckillActivityList(@Param("status") Integer status);

    /**
     * 根据时间和状态获取秒杀活动列表
     */
    List<SeckillActivity> getSeckillActivityListBetweenStartTimeAndEndTime(@Param("current") LocalDateTime current,
                                                                           @Param("status") Integer status);

    /**
     * 获取秒杀活动详情
     */
    SeckillActivity getSeckillActivityById(@Param("activityId") Long activityId);

    /**
     * 更新秒杀活动状态
     */
    void updateSeckillActivityStatus(@Param("activityId") Long activityId, @Param("status") Integer status);
}
