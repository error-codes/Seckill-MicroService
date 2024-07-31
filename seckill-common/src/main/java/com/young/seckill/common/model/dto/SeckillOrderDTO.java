package com.young.seckill.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7358875355301726116L;

    // 订单ID
    private Long id;

    // 用户ID
    private Long userId;

    // 商品ID
    private Long goodsId;

    // 购买数量
    private Integer quantity;

    // 活动ID
    private Long activityId;

}
