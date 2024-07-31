package com.young.seckill.stock.application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillStockBucketGoodsCommand implements Serializable {


    @Serial
    private static final long serialVersionUID = 3239399548871911837L;

    // 用户ID
    private Long userId;

    // 商品ID
    private Long goodsId;

    public boolean isEmpty() {
        return this.goodsId == null || this.userId == null;
    }
}
