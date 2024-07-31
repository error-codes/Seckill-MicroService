package com.young.seckill.goods.domain.service;

import com.young.seckill.goods.domain.entity.SeckillGoods;

import java.util.List;

public interface SeckillGoodsDomainService {
    /**
     * 保存商品信息
     *
     * @param seckillGoods 商品信息
     */
    void saveSeckillGoods(SeckillGoods seckillGoods);

    /**
     * 根据商品ID获取商品详情
     *
     * @param goodsId 商品ID
     */
    SeckillGoods getSeckillGoodsById(Long goodsId);

    /**
     * 根据活动ID获取商品列表
     *
     * @param activityId 活动ID
     */
    List<SeckillGoods> getSeckillGoodsByActivityId(Long activityId);

    /**
     * 修改商品状态
     *
     * @param goodsId 商品ID
     * @param status  新的商品状态
     */
    void updateGoodsStatus(Long goodsId, Integer status);

    /**
     * 扣减指定商品的库存
     *
     * @param goodsId  商品ID
     * @param quantity 购买数量
     */
    boolean updateGoodsAvailableStock(Long goodsId, Integer quantity);

    /**
     * 增加指定商品的库存
     *
     * @param goodsId 商品ID
     * @param stock   要增加的库存数量
     */
    boolean incrementAvailableStock(Long goodsId, Integer stock);

    /**
     * 获取当前可用库存
     *
     * @param goodsId 商品ID
     */
    Integer getAvailableStockById(Long goodsId);
}