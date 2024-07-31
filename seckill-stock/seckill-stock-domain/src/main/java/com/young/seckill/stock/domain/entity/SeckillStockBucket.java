package com.young.seckill.stock.domain.entity;

import com.young.seckill.common.utils.SnowFlakeFactory;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class SeckillStockBucket implements Serializable {

    @Serial
    private static final long serialVersionUID = 8225523587093921070L;

    // 库存ID
    private Long id;

    // 商品ID
    private Long goodsId;

    // 初始库存
    private Integer initialStock;

    // 可用库存
    private Integer availableStock;

    // 库存状态【0-不可用；1-可用】
    private Integer status;

    // 分桶编号
    private Integer serialNo;

    public SeckillStockBucket(Long goodsId, Integer initialStock, Integer availableStock, Integer status, Integer serialNo) {
        this.id = SnowFlakeFactory.getSnowFlakeIDCache().nextId();
        this.goodsId = goodsId;
        this.initialStock = initialStock;
        this.availableStock = availableStock;
        this.status = status;
        this.serialNo = serialNo;
    }
}
