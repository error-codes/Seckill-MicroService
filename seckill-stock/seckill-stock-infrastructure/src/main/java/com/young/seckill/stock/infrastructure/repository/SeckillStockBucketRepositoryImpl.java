package com.young.seckill.stock.infrastructure.repository;

import com.young.seckill.common.model.enums.SeckillStockBucketStatus;
import com.young.seckill.stock.infrastructure.mapper.SeckillStockBucketMapper;
import com.young.seckill.stock.domain.entity.SeckillStockBucket;
import com.young.seckill.stock.domain.repository.SeckillStockBucketRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Repository
public class SeckillStockBucketRepositoryImpl implements SeckillStockBucketRepository {

    private final SeckillStockBucketMapper seckillStockBucketMapper;

    public SeckillStockBucketRepositoryImpl(SeckillStockBucketMapper seckillStockBucketMapper) {
        this.seckillStockBucketMapper = seckillStockBucketMapper;
    }

    @Override
    public boolean suspendBuckets(Long goodsId) {
        if (Objects.isNull(goodsId)) {
            return false;
        }
        seckillStockBucketMapper.updateStatusByGoodsId(SeckillStockBucketStatus.DISABLED.getCode(), goodsId);
        return true;
    }

    @Override
    public boolean resumeBuckets(Long goodsId) {
        if (Objects.isNull(goodsId)) {
            return false;
        }
        seckillStockBucketMapper.updateStatusByGoodsId(SeckillStockBucketStatus.ENABLED.getCode(), goodsId);
        return true;
    }

    @Override
    public List<SeckillStockBucket> getBucketByGoodsId(Long goodsId) {
        if (Objects.isNull(goodsId)) {
            return List.of();
        }
        return seckillStockBucketMapper.getBucketsByGoodsId(goodsId);
    }

    @Override
    public boolean submitBuckets(Long goodsId, List<SeckillStockBucket> buckets) {
        if (Objects.isNull(goodsId) || CollectionUtils.isEmpty(buckets)) {
            return false;
        }
        seckillStockBucketMapper.deleteByGoodsId(goodsId);
        seckillStockBucketMapper.insertBatch(buckets);
        return true;
    }

    @Override
    public boolean decreaseStockt(Integer quantity, Integer serialNo, Long goodsId) {
        if (Objects.isNull(quantity) || Objects.isNull(serialNo) || Objects.isNull(goodsId)) {
            return false;
        }
        seckillStockBucketMapper.decreaseStock(quantity, serialNo, goodsId);
        return true;
    }

    @Override
    public boolean increaseStock(Integer quantity, Integer serialNo, Long goodsId) {
        if (Objects.isNull(quantity) || Objects.isNull(serialNo) || Objects.isNull(goodsId)) {
            return false;
        }
        seckillStockBucketMapper.increaseStock(quantity, serialNo, goodsId);
        return true;
    }
}
