package com.young.seckill.dubbo.interfaces.goods;

import com.young.seckill.common.model.dto.SeckillGoodsDTO;

public interface SeckillGoodsDubboService {

    /**
     * 根据id和版本号获取商品详情
     *
     * @param goodsId 商品ID
     * @param version 版本号
     */
    SeckillGoodsDTO getSeckillGoods(Long goodsId, Long version);

    /**
     * 扣减数据库库存
     *
     * @param goodsId  商品ID
     * @param quantity 购买数量
     */
    boolean updateGoodsAvailableStock(Long goodsId, Integer quantity);


    /**
     * 根据商品ID获取可用库存
     *
     * @param goodsId 商品ID
     */
    Integer getAvailableStockByGoodsId(Long goodsId);

}
