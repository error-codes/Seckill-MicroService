package com.young.seckill.stock.domain.service;

import com.young.seckill.stock.domain.entity.SeckillStockBucket;
import com.young.seckill.stock.domain.entity.SeckillStockBucketDeduction;

import java.util.List;

public interface SeckillStockBucketDomainService {

    /**
     * 关闭库存分桶
     *
     * @param goodsId 商品ID
     */
    boolean suspendBuckets(Long goodsId);

    /**
     * 恢复库存分桶
     *
     * @param goodsId 商品ID
     */
    boolean resumeBuckets(Long goodsId);

    /**
     * 根据商品ID获取库存分桶列表
     *
     * @param goodsId 商品ID
     */
    List<SeckillStockBucket> getBucketByGoodsId(Long goodsId);

    /**
     * 库存扣减
     *
     * @param seckillStockBucketDeduction 库存扣减信息
     */
    boolean decreaseStockt(SeckillStockBucketDeduction seckillStockBucketDeduction);

    /**
     * 增加库存
     *
     * @param seckillStockBucketDeduction 库存扣减信息
     */
    boolean increaseStock(SeckillStockBucketDeduction seckillStockBucketDeduction);


    /**
     * 编排库存分桶
     */
    boolean arrangeBuckets(Long goodsId, List<SeckillStockBucket> buckets);

}
