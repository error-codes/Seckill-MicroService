package com.young.seckill.activity.application.dubbo;

import com.young.seckill.activity.application.service.SeckillActivityService;
import com.young.seckill.common.model.dto.SeckillActivityDTO;
import com.young.seckill.dubbo.interfaces.activity.SeckillActivityDubboService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;

@Component
@DubboService(version = "1.0.0")
public class SeckillActivityDubboServiceImpl implements SeckillActivityDubboService {

    private final SeckillActivityService seckillActivityService;

    public SeckillActivityDubboServiceImpl(SeckillActivityService seckillActivityService) {
        this.seckillActivityService = seckillActivityService;
    }

    @Override
    public SeckillActivityDTO getSeckillActivity(Long activityId, Long version) {
        return seckillActivityService.getSeckillActivityById(activityId, version);
    }
}
