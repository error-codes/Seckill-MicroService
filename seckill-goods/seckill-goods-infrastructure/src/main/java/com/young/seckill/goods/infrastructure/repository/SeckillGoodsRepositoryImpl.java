package com.young.seckill.goods.infrastructure.repository;

import com.young.seckill.goods.domain.entity.SeckillGoods;
import com.young.seckill.goods.domain.repository.SeckillGoodsRepository;
import com.young.seckill.goods.infrastructure.mapper.SeckillGoodsMapper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SeckillGoodsRepositoryImpl implements SeckillGoodsRepository {

    private final SeckillGoodsMapper seckillGoodsMapper;

    @Lazy
    public SeckillGoodsRepositoryImpl(SeckillGoodsMapper seckillGoodsMapper) {
        this.seckillGoodsMapper = seckillGoodsMapper;
    }

    @Override
    public void saveSeckillGoods(SeckillGoods seckillGoods) {
        seckillGoodsMapper.saveSeckillGoods(seckillGoods);
    }

    @Override
    public SeckillGoods getSeckillGoodsById(Long goodsId) {
        return seckillGoodsMapper.getSeckillGoodsById(goodsId);
    }

    @Override
    public List<SeckillGoods> getSeckillGoodsByActivityId(Long activityId) {
        return seckillGoodsMapper.getSeckillGoodsByActivityId(activityId);
    }

    @Override
    public Integer updateGoodsStatus(Long goodsId, Integer status) {
        return seckillGoodsMapper.updateGoodsStatus(goodsId, status);
    }

    @Override
    public Integer updateGoodsAvailableStock(Long goodsId, Integer quantity) {
        return seckillGoodsMapper.updateGoodsAvailableStock(goodsId, quantity);
    }

    @Override
    public Integer incrementAvailableStock(Long goodsId, Integer stock) {
        return seckillGoodsMapper.incrementAvailableStock(goodsId, stock);
    }

    @Override
    public Integer getAvailableStockById(Long goodsId) {
        return seckillGoodsMapper.getAvailableStockById(goodsId);
    }
}
