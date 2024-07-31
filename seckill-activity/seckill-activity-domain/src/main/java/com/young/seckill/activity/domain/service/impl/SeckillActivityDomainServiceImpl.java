package com.young.seckill.activity.domain.service.impl;

import com.young.seckill.MessageSenderService;
import com.young.seckill.activity.domain.entity.SeckillActivity;
import com.young.seckill.activity.domain.event.SeckillActivityEvent;
import com.young.seckill.activity.domain.repository.SeckillActivityRepository;
import com.young.seckill.activity.domain.service.SeckillActivityDomainService;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.exception.ExceptionChecker;
import com.young.seckill.common.model.enums.SeckillActivityStatus;
import com.young.seckill.common.response.RespCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeckillActivityDomainServiceImpl implements SeckillActivityDomainService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillActivityDomainServiceImpl.class);

    private final SeckillActivityRepository seckillActivityRepository;
    private final MessageSenderService      messageSenderService;

    public SeckillActivityDomainServiceImpl(SeckillActivityRepository seckillActivityRepository,
                                            MessageSenderService messageSenderService) {
        this.seckillActivityRepository = seckillActivityRepository;
        this.messageSenderService = messageSenderService;
    }

    @Override
    public void saveSeckillActivity(SeckillActivity seckillActivity) {
        String eventName = "ACTIVITY_PUBLISH";
        LOGGER.info("{}|发布秒杀活动|{}", eventName, seckillActivity);

        ExceptionChecker.throwAssertIfNullOrEmpty(seckillActivity, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfBlank(seckillActivity.getActivityDesc(), RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfBlank(seckillActivity.getActivityName(), RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(seckillActivity.getStartTime(), RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(seckillActivity.getEndTime(), RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfFalse(seckillActivity.getStartTime().isBefore(seckillActivity.getEndTime()),
                                            RespCode.PARAMS_INVALID);

        seckillActivity.setStatus(SeckillActivityStatus.PUBLISHED.getCode());
        seckillActivityRepository.saveSeckillActivity(seckillActivity);

        LOGGER.info("{}|秒杀活动已经发布|{}", eventName, seckillActivity.getId());

        SeckillActivityEvent seckillActivityEvent =
                new SeckillActivityEvent(eventName, seckillActivity.getId(), seckillActivity.getStatus(),
                                         SeckillConstants.EVENT_TOPIC_ACTIVITY_KEY);
        messageSenderService.sendMessage(seckillActivityEvent);
    }

    @Override
    public List<SeckillActivity> getSeckillActivityList(Integer status) {
        return seckillActivityRepository.getSeckillActivityList(status);
    }

    @Override
    public List<SeckillActivity> getSeckillActivityListBetweenStartTimeAndEndTime(LocalDateTime current, Integer status) {
        return seckillActivityRepository.getSeckillActivityListBetweenStartTimeAndEndTime(current, status);
    }

    @Override
    public SeckillActivity getSeckillActivityById(Long activityId) {
        return seckillActivityRepository.getSeckillActivityById(activityId);
    }

    @Override
    public void updateSeckillActivityStatus(Long activityId, Integer status) {
        String eventName = "ACTIVITY_UPDATE";
        LOGGER.info("{}|修改秒杀活动状态|{} {}", eventName, activityId, status);

        ExceptionChecker.throwAssertIfNullOrEmpty(activityId, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(status, RespCode.PARAMS_INVALID);

        SeckillActivity seckillActivity = seckillActivityRepository.getSeckillActivityById(activityId);

        ExceptionChecker.throwAssertIfNullOrEmpty(seckillActivity, RespCode.ACTIVITY_NOT_EXISTS);

        seckillActivityRepository.updateSeckillActivityStatus(activityId, status);
        LOGGER.info("{}|秒杀活动状态已修改|{} {}", eventName, activityId, status);

        SeckillActivityEvent seckillActivityEvent =
                new SeckillActivityEvent(eventName, activityId, status, SeckillConstants.EVENT_TOPIC_ACTIVITY_KEY);
        messageSenderService.sendMessage(seckillActivityEvent);
    }
}
