package com.young.seckill.common.model.rocketmq;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ExceptionTopicMessage extends BasicTopicMessage {

    // 全局事务编号
    private Long txNo;

    // 商品ID
    private Long goodsId;

    // 购买数量
    private Integer quantity;

    // 下单类型
    private String orderPlaceType;

    // 是否异常
    private Boolean exception;

    public ExceptionTopicMessage(String destination,
                                 Long txNo,
                                 Long goodsId,
                                 Integer quantity,
                                 String orderPlaceType,
                                 Boolean exception) {
        super(destination);
        this.txNo = txNo;
        this.goodsId = goodsId;
        this.quantity = quantity;
        this.orderPlaceType = orderPlaceType;
        this.exception = exception;
    }
}
