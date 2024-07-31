package com.young.seckill.common.model.rocketmq;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class TransactionTopicMessage extends ExceptionTopicMessage {

    // 活动ID
    private Long activityId;

    // 商品版本号
    private Long version;

    // 用户ID
    private Long userId;

    // 商品名称
    private String goodsName;

    // 秒杀活动价格
    private BigDecimal activityPrice;

    public TransactionTopicMessage(String destination,
                                   Long txNo,
                                   Long goodsId,
                                   Integer quantity,
                                   String orderPlaceType,
                                   Boolean exception,
                                   Long activityId,
                                   Long version,
                                   Long userId,
                                   String goodsName,
                                   BigDecimal activityPrice) {
        super(destination, txNo, goodsId, quantity, orderPlaceType, exception);
        this.activityId = activityId;
        this.version = version;
        this.userId = userId;
        this.goodsName = goodsName;
        this.activityPrice = activityPrice;
    }
}
