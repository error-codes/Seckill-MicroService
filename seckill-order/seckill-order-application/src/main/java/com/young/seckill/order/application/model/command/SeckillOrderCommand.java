package com.young.seckill.order.application.model.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderCommand implements Serializable {

    @Serial
    private static final long serialVersionUID = 4439607681951811748L;

    // 商品ID
    private Long goodsId;

    // 购买数量
    private Integer quantity;

    // 活动ID
    private Long activityId;

    // 商品版本号
    private Long version;

}
