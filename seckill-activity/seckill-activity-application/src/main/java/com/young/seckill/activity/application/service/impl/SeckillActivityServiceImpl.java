package com.young.seckill.activity.application.service.impl;

import com.young.seckill.activity.application.cache.service.SeckillActivityCacheService;
import com.young.seckill.activity.application.cache.service.SeckillActivityListCacheService;
import com.young.seckill.activity.application.service.SeckillActivityService;
import com.young.seckill.activity.domain.entity.SeckillActivity;
import com.young.seckill.activity.domain.service.SeckillActivityDomainService;
import com.young.seckill.common.cache.model.SeckillBusinessCache;
import com.young.seckill.common.exception.ExceptionChecker;
import com.young.seckill.common.model.dto.SeckillActivityDTO;
import com.young.seckill.common.model.enums.SeckillActivityStatus;
import com.young.seckill.common.response.RespCode;
import com.young.seckill.common.utils.SnowFlakeFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeckillActivityServiceImpl implements SeckillActivityService {

    private final SeckillActivityDomainService    seckillActivityDomainService;
    private final SeckillActivityCacheService     seckillActivityCacheService;
    private final SeckillActivityListCacheService seckillActivityListCacheService;

    public SeckillActivityServiceImpl(SeckillActivityDomainService seckillActivityDomainService,
                                      SeckillActivityCacheService seckillActivityCacheService,
                                      SeckillActivityListCacheService seckillActivityListCacheService) {
        this.seckillActivityCacheService = seckillActivityCacheService;
        this.seckillActivityDomainService = seckillActivityDomainService;
        this.seckillActivityListCacheService = seckillActivityListCacheService;
    }

    @Override
    public void saveSeckillActivity(SeckillActivityDTO seckillActivityDTO) {
        SeckillActivity seckillActivity = new SeckillActivity();
        BeanUtils.copyProperties(seckillActivityDTO, seckillActivity);
        seckillActivity.setId(SnowFlakeFactory.getSnowFlakeIDCache().nextId());
        seckillActivity.setStatus(SeckillActivityStatus.PUBLISHED.getCode());
        seckillActivityDomainService.saveSeckillActivity(seckillActivity);
    }

    @Override
    public List<SeckillActivity> getSeckillActivityList(Integer status) {
        return seckillActivityDomainService.getSeckillActivityList(status);
    }

    @Override
    public List<SeckillActivityDTO> getSeckillActivityList(Long version, Integer status) {
        SeckillBusinessCache<List<SeckillActivity>> seckillActivityListCache =
                seckillActivityListCacheService.getCacheActivities(status, version);

        ExceptionChecker.throwAssertIfFalse(seckillActivityListCache.isExist(), RespCode.ACTIVITY_NOT_EXISTS);
        // 稍后再试，前端需要对状态做特殊处理，不去刷新数据，静默稍后再试
        ExceptionChecker.throwAssertIfTrue(seckillActivityListCache.isRetryLater(), RespCode.RETRY_LATER);

        return seckillActivityListCache.getData().stream().map(seckillActivity -> {
            SeckillActivityDTO seckillActivityDTO = new SeckillActivityDTO();
            BeanUtils.copyProperties(seckillActivity, seckillActivityDTO);
            seckillActivityDTO.setVersion(seckillActivityListCache.getVersion());
            return seckillActivityDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<SeckillActivity> getSeckillActivityListBetweenStartTimeAndEndTime(LocalDateTime current, Integer status) {
        return seckillActivityDomainService.getSeckillActivityListBetweenStartTimeAndEndTime(current, status);
    }

    @Override
    public List<SeckillActivityDTO> getSeckillActivityListBetweenStartTimeAndEndTime(LocalDateTime current,
                                                                                     Integer status,
                                                                                     Long version) {
        SeckillBusinessCache<List<SeckillActivity>> seckillActivityListCache =
                seckillActivityListCacheService.getCacheActivities(current, status, version);

        ExceptionChecker.throwAssertIfFalse(seckillActivityListCache.isExist(), RespCode.ACTIVITY_NOT_EXISTS);
        // 稍后再试，前端需要对状态做特殊处理，不去刷新数据，静默稍后再试
        ExceptionChecker.throwAssertIfTrue(seckillActivityListCache.isRetryLater(), RespCode.RETRY_LATER);

        return seckillActivityListCache.getData().stream().map(seckillActivity -> {
            SeckillActivityDTO seckillActivityDTO = new SeckillActivityDTO();
            BeanUtils.copyProperties(seckillActivity, seckillActivityDTO);
            seckillActivityDTO.setVersion(seckillActivityListCache.getVersion());
            return seckillActivityDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public SeckillActivity getSeckillActivityById(Long activityId) {
        return seckillActivityDomainService.getSeckillActivityById(activityId);
    }

    @Override
    public SeckillActivityDTO getSeckillActivityById(Long activityId, Long version) {
        SeckillBusinessCache<SeckillActivity> seckillActivityCache = seckillActivityCacheService.getCacheActivity(activityId, version);

        ExceptionChecker.throwAssertIfFalse(seckillActivityCache.isExist(), RespCode.ACTIVITY_NOT_EXISTS);
        // 稍后再试，前端需要对状态做特殊处理，不去刷新数据，静默稍后再试
        ExceptionChecker.throwAssertIfTrue(seckillActivityCache.isRetryLater(), RespCode.RETRY_LATER);

        SeckillActivityDTO seckillActivityDTO = new SeckillActivityDTO();
        BeanUtils.copyProperties(seckillActivityCache.getData(), seckillActivityDTO);
        seckillActivityDTO.setVersion(seckillActivityCache.getVersion());
        return seckillActivityDTO;
    }

    @Override
    public void updateSeckillActivityStatus(Long activityId, Integer status) {
        seckillActivityDomainService.updateSeckillActivityStatus(activityId, status);
    }
}
