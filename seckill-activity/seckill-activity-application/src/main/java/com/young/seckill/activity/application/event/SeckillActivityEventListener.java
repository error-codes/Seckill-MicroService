package com.young.seckill.activity.application.event;

import com.young.seckill.activity.application.cache.service.SeckillActivityCacheService;
import com.young.seckill.activity.application.cache.service.SeckillActivityListCacheService;
import com.young.seckill.activity.domain.event.SeckillActivityEvent;
import com.young.seckill.common.response.RespResult;
import com.young.seckill.common.utils.JACKSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SeckillActivityEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillActivityEventListener.class);

    private final SeckillActivityCacheService     activityCacheService;
    private final SeckillActivityListCacheService activityListCacheService;

    public SeckillActivityEventListener(SeckillActivityCacheService activityCacheService,
                                        SeckillActivityListCacheService activityListCacheService) {
        this.activityCacheService = activityCacheService;
        this.activityListCacheService = activityListCacheService;
    }

    @EventListener
    public RespResult<String> execute(SeckillActivityEvent event) {
        LOGGER.info("Spring|ActivityEvent 接收活动事件|{}", JACKSON.toJson(event));

        activityCacheService.tryUpdateSeckillActivityCacheByLock(event.getActivityId(), false);
        activityListCacheService.tryUpdateSeckillActivityCacheByLock(event.getStatus(), false);

        return RespResult.success();
    }

}
