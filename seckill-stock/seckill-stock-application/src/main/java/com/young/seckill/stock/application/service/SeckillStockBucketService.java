package com.young.seckill.stock.application.service;

import com.young.seckill.stock.application.model.SeckillStockBucketDTO;
import com.young.seckill.stock.application.model.SeckillStockBucketWrapperCommand;

public interface SeckillStockBucketService {

    /**
     * 编排库存
     *
     * @param userId                    用户ID
     * @param stockBucketWrapperCommand 商品库存分桶信息
     */
    void arrangeStockBuckets(Long userId, SeckillStockBucketWrapperCommand stockBucketWrapperCommand);

    /**
     * 获取全部库存
     *
     * @param goodsId 商品ID
     * @param version 版本号
     */
    SeckillStockBucketDTO getTotalStockBuckets(Long goodsId, Long version);
}
