package com.young.seckill.goods.infrastructure.mapper;

import com.young.seckill.goods.domain.entity.SeckillGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SeckillGoodsMapper {

    /**
     * 保存商品信息
     */
    void saveSeckillGoods(SeckillGoods seckillGoods);

    /**
     * 根据商品ID获取商品详情
     */
    SeckillGoods getSeckillGoodsById(@Param("goodsId") Long goodsId);

    /**
     * 根据活动ID获取商品列表
     */
    List<SeckillGoods> getSeckillGoodsByActivityId(@Param("activityId") Long activityId);

    /**
     * 修改商品状态
     */
    Integer updateGoodsStatus(@Param("goodsId") Long goodsId, @Param("status") Integer status);

    /**
     * 扣减指定商品的库存
     */
    Integer updateGoodsAvailableStock(@Param("goodsId") Long goodsId, @Param("quantity") Integer quantity);

    /**
     * 增加指定商品的库存
     */
    Integer incrementAvailableStock(@Param("goodsId") Long goodsId, @Param("stock") Integer stock);

    /**
     * 获取当前可用库存
     */
    Integer getAvailableStockById(@Param("goodsId") Long goodsId);
}
