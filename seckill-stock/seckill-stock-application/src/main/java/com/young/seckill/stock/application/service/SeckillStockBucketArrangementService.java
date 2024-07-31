package com.young.seckill.stock.application.service;

import com.young.seckill.stock.application.model.SeckillStockBucketDTO;

public interface SeckillStockBucketArrangementService {

    /**
     * 编码分桶库存
     *
     * @param goodsId        商品ID
     * @param stock          库存量
     * @param bucketQuantity 分桶数量
     * @param assignmentMode 编排模式【1-总量模式；2-增量模式】
     */
    void arrangeStockBuckets(Long goodsId, Integer stock, Integer bucketQuantity, Integer assignmentMode);

    /**
     * 通过商品ID获取库存分桶数据
     *
     * @param goodsId 商品ID
     * @param version 版本号
     */
    SeckillStockBucketDTO getSeckillStockBucketDTO(Long goodsId, Long version);
}
