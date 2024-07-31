package com.young.seckill.common.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillSubmitOrderDTO {

    /**
     * 同步下单时，为订单ID
     * 异步下单时，为许可ID
     */
    private String id;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 类型
     * type_order：ID为订单号
     * type_task：ID为下单许可号
     */
    private String type;

}
