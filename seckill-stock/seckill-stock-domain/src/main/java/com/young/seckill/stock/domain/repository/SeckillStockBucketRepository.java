package com.young.seckill.stock.domain.repository;

import com.young.seckill.stock.domain.entity.SeckillStockBucket;

import java.util.List;

public interface SeckillStockBucketRepository {

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
     * 批量提交商品库存信息
     *
     * @param goodsId 商品ID
     * @param buckets 库存分桶列表
     */
    boolean submitBuckets(Long goodsId, List<SeckillStockBucket> buckets);

    /**
     * 库存扣减
     *
     * @param quantity 购买数量
     * @param serialNo 事务编号
     * @param goodsId  商品ID
     */
    boolean decreaseStockt(Integer quantity, Integer serialNo, Long goodsId);

    /**
     * 增加库存
     *
     * @param quantity 购买数量
     * @param serialNo 事务编号
     * @param goodsId  商品ID
     */
    boolean increaseStock(Integer quantity, Integer serialNo, Long goodsId);
}
