package com.young.seckill.dubbo.interfaces.activity;

import com.young.seckill.common.model.dto.SeckillActivityDTO;

public interface SeckillActivityDubboService {

    /**
     * 获取活动信息
     *
     * @param activityId 活动ID
     * @param version    版本号
     */
    SeckillActivityDTO getSeckillActivity(Long activityId, Long version);
}
