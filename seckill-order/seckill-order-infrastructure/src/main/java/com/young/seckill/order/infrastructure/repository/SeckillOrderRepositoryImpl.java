package com.young.seckill.order.infrastructure.repository;

import com.young.seckill.order.domain.entity.SeckillOrder;
import com.young.seckill.order.domain.repository.SeckillOrderRepository;
import com.young.seckill.order.infrastructure.mapper.SeckillOrderMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SeckillOrderRepositoryImpl implements SeckillOrderRepository {

    private final SeckillOrderMapper seckillOrderMapper;


    public SeckillOrderRepositoryImpl(SeckillOrderMapper seckillOrderMapper) {
        this.seckillOrderMapper = seckillOrderMapper;
    }

    @Override
    public boolean saveSeckillOrder(SeckillOrder seckillOrder) {
        return seckillOrderMapper.saveSeckillOrder(seckillOrder);
    }

    @Override
    public List<SeckillOrder> getSeckillOrderByUserId(Long userId) {
        return seckillOrderMapper.getSeckillOrderByUserId(userId);
    }

    @Override
    public List<SeckillOrder> getSeckillOrderByActivityId(Long activityId) {
        return seckillOrderMapper.getSeckillOrderByActivityId(activityId);
    }

    @Override
    public boolean deleteSeckillOrder(Long orderId) {
        return seckillOrderMapper.deleteSeckillOrder(orderId);
    }
}
