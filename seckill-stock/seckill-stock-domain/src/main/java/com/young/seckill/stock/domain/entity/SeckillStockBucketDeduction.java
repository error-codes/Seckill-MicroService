package com.young.seckill.stock.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillStockBucketDeduction implements Serializable {

    @Serial
    private static final long serialVersionUID = -7061568533561610902L;

    // 商品ID
    private Long goodsId;

    // 商品数量
    private Integer quantity;

    // 用户ID
    private Long userId;

    // 分桶编号
    private Integer serialNo;
}
