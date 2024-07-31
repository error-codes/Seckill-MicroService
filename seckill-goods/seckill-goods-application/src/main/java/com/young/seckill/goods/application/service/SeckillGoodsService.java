package com.young.seckill.goods.application.service;


import com.young.seckill.common.model.dto.SeckillGoodsDTO;
import com.young.seckill.common.model.rocketmq.TransactionTopicMessage;
import com.young.seckill.goods.application.command.SeckillGoodsCommand;
import com.young.seckill.goods.domain.entity.SeckillGoods;

import java.util.List;

public interface SeckillGoodsService {

    /**
     * 保存商品信息
     *
     * @param seckillGoodsCommand 商品参数
     */
    void saveSeckillGoods(SeckillGoodsCommand seckillGoodsCommand);

    /**
     * 根据商品ID获取商品详情
     *
     * @param goodsId 商品ID
     */
    SeckillGoods getSeckillGoodsById(Long goodsId);

    /**
     * 根据商品ID以及版本号获取商品详情
     *
     * @param goodsId 商品ID
     * @param version 版本号
     */
    SeckillGoodsDTO getSeckillGoodsById(Long goodsId, Long version);

    /**
     * 根据活动ID获取商品列表
     *
     * @param activityId 活动ID
     */
    List<SeckillGoods> getSeckillGoodsByActivityId(Long activityId);

    /**
     * 根据活动ID以及版本号获取商品列表
     *
     * @param activityId 活动ID
     * @param version    版本号
     */
    List<SeckillGoodsDTO> getSeckillGoodsByActivityId(Long activityId, Long version);

    /**
     * 修改商品状态
     *
     * @param goodsId 商品ID
     * @param status  商品状态
     */
    void updateGoodsStatus(Long goodsId, Integer status);

    /**
     * 增加指定商品库存
     *
     * @param goodsId 商品ID
     * @param stock   商品库存
     */
    boolean incrementGoodsAvailableStock(Long goodsId, Integer stock);

    /**
     * 扣减指定商品的库存
     *
     * @param goodsId  商品ID
     * @param quantity 购买数量
     */
    boolean updateGoodsAvailableStock(Long goodsId, Integer quantity);

    /**
     * 根据事务消息扣减商品库存
     *
     * @param transactionTopicMessage 事务消息
     */
    boolean updateGoodsAvailableStock(TransactionTopicMessage transactionTopicMessage);

    /**
     * 获取当前可用库存
     *
     * @param goodsId 商品ID
     */
    Integer getAvailableStockByGoodsId(Long goodsId);
}
