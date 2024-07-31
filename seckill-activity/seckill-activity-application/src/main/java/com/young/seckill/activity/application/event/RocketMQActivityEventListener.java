package com.young.seckill.activity.application.event;


import com.young.seckill.activity.application.cache.service.SeckillActivityCacheService;
import com.young.seckill.activity.application.cache.service.SeckillActivityListCacheService;
import com.young.seckill.activity.domain.event.SeckillActivityEvent;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.utils.JACKSON;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(name = "event.publish.type", havingValue = "rocketmq")
@RocketMQMessageListener(consumerGroup = SeckillConstants.EVENT_ACTIVITY_CONSUMER_GROUP,
                         topic = SeckillConstants.EVENT_TOPIC_ACTIVITY_KEY)
public class RocketMQActivityEventListener implements RocketMQListener<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQActivityEventListener.class);

    private final SeckillActivityCacheService     activityCacheService;
    private final SeckillActivityListCacheService activityListCacheService;

    public RocketMQActivityEventListener(SeckillActivityCacheService activityCacheService,
                                         SeckillActivityListCacheService activityListCacheService) {
        this.activityCacheService = activityCacheService;
        this.activityListCacheService = activityListCacheService;
    }

    @Override
    public void onMessage(String message) {
        LOGGER.info("RocketMQ|ActivityEvent 接收活动事件|{}", message);
        if (!StringUtils.hasText(message)) {
            LOGGER.info("RocketMQ|ActivityEvent|事件参数错误");
            return;
        }
        SeckillActivityEvent seckillActivityEvent = getEventMessage(message);

        activityCacheService.tryUpdateSeckillActivityCacheByLock(seckillActivityEvent.getActivityId(), false);
        activityListCacheService.tryUpdateSeckillActivityCacheByLock(seckillActivityEvent.getStatus(), false);
    }

    private SeckillActivityEvent getEventMessage(String message) {
        return JACKSON.toObj(message, SeckillActivityEvent.class);
    }
}
