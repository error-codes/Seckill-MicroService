package com.young.seckill.activity.domain.service;


import com.young.seckill.activity.domain.entity.SeckillActivity;

import java.time.LocalDateTime;
import java.util.List;

public interface SeckillActivityDomainService {

    /**
     * 保存秒杀活动
     *
     * @param seckillActivity 活动参数
     */
    void saveSeckillActivity(SeckillActivity seckillActivity);

    /**
     * 根据状态获取秒杀活动列表
     *
     * @param status 活动状态
     */
    List<SeckillActivity> getSeckillActivityList(Integer status);


    /**
     * 根据时间和状态获取秒杀活动列表
     *
     * @param current 当前时间
     * @param status  活动状态
     */
    List<SeckillActivity> getSeckillActivityListBetweenStartTimeAndEndTime(LocalDateTime current, Integer status);

    /**
     * 根据活动ID获取秒杀活动详情
     *
     * @param activityId 活动ID
     */
    SeckillActivity getSeckillActivityById(Long activityId);


    /**
     * 更新秒杀活动状态
     *
     * @param activityId 活动ID
     * @param status     活动状态
     */
    void updateSeckillActivityStatus(Long activityId, Integer status);
}
