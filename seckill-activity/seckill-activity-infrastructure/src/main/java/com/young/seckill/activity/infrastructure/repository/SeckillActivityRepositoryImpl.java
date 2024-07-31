package com.young.seckill.activity.infrastructure.repository;

import com.young.seckill.activity.domain.entity.SeckillActivity;
import com.young.seckill.activity.domain.repository.SeckillActivityRepository;
import com.young.seckill.activity.infrastructure.mapper.SeckillActivityMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class SeckillActivityRepositoryImpl implements SeckillActivityRepository {

    private final SeckillActivityMapper seckillActivityMapper;

    public SeckillActivityRepositoryImpl(SeckillActivityMapper seckillActivityMapper) {
        this.seckillActivityMapper = seckillActivityMapper;
    }

    @Override
    public void saveSeckillActivity(SeckillActivity seckillActivity) {
        seckillActivityMapper.saveSeckillActivity(seckillActivity);
    }

    @Override
    public List<SeckillActivity> getSeckillActivityList(Integer status) {
        return seckillActivityMapper.getSeckillActivityList(status);
    }

    @Override
    public List<SeckillActivity> getSeckillActivityListBetweenStartTimeAndEndTime(LocalDateTime current, Integer status) {
        return seckillActivityMapper.getSeckillActivityListBetweenStartTimeAndEndTime(current, status);
    }

    @Override
    public SeckillActivity getSeckillActivityById(Long activityId) {
        return seckillActivityMapper.getSeckillActivityById(activityId);
    }

    @Override
    public void updateSeckillActivityStatus(Long activityId, Integer status) {
        seckillActivityMapper.updateSeckillActivityStatus(activityId, status);
    }
}
