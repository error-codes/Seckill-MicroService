package com.young.seckill.activity.application.service;

import com.young.seckill.activity.domain.entity.SeckillActivity;
import com.young.seckill.common.model.dto.SeckillActivityDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface SeckillActivityService {

    /**
     * 保存秒杀活动
     *
     * @param seckillActivityDTO 活动参数
     */
    void saveSeckillActivity(SeckillActivityDTO seckillActivityDTO);

    /**
     * 根据状态获取秒杀活动列表
     *
     * @param status 活动状态
     */
    List<SeckillActivity> getSeckillActivityList(Integer status);

    /**
     * 根据状态和版本号获取秒杀活动列表
     *
     * @param version 版本号
     * @param status  活动状态
     */
    List<SeckillActivityDTO> getSeckillActivityList(Long version, Integer status);

    /**
     * 根据时间和状态获取秒杀活动列表
     *
     * @param current 当前时间
     * @param status  活动状态
     */
    List<SeckillActivity> getSeckillActivityListBetweenStartTimeAndEndTime(LocalDateTime current, Integer status);

    /**
     * 根据时间和状态以及版本号获取秒杀活动列表
     *
     * @param current 当前时间
     * @param status  活动状态
     */
    List<SeckillActivityDTO> getSeckillActivityListBetweenStartTimeAndEndTime(LocalDateTime current, Integer status, Long version);

    /**
     * 根据活动ID获取秒杀活动详情
     *
     * @param activityId 活动ID
     */
    SeckillActivity getSeckillActivityById(Long activityId);

    /**
     * 根据活动ID以及版本号获取秒杀活动详情
     *
     * @param activityId 活动ID
     * @param version    版本号
     */
    SeckillActivityDTO getSeckillActivityById(Long activityId, Long version);

    /**
     * 更新秒杀活动状态
     *
     * @param activityId 活动ID
     * @param status     活动状态
     */
    void updateSeckillActivityStatus(Long activityId, Integer status);
}
