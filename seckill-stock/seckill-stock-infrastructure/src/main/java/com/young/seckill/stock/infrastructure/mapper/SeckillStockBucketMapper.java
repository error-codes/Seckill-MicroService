package com.young.seckill.stock.infrastructure.mapper;

import com.young.seckill.stock.domain.entity.SeckillStockBucket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SeckillStockBucketMapper {

    /**
     * 增加库存
     */
    int increaseStock(@Param("quantity") Integer quantity, @Param("serialNo") Integer serialNo, @Param("goodsId") Long goodsId);

    /**
     * 扣减库存
     */
    int decreaseStock(@Param("quantity") Integer quantity, @Param("serialNo") Integer serialNo, @Param("goodsId") Long goodsId);

    /**
     * 根据商品id获取库存分桶列表
     */
    List<SeckillStockBucket> getBucketsByGoodsId(@Param("goodsId") Long goodsId);

    /**
     * 根据商品id修改状态
     */
    int updateStatusByGoodsId(@Param("status") Integer status, @Param("goodsId") Long goodsId);

    /**
     * 根据商品id删除数据
     */
    int deleteByGoodsId(@Param("goodsId") Long goodsId);

    /**
     * 保存分桶数据
     */
    void insertBatch(@Param("buckets") List<SeckillStockBucket> buckets);
}
