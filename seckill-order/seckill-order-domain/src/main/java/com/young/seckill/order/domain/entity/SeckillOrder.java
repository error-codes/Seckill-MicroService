package com.young.seckill.order.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 8896854182049453262L;

    // 订单id
    private Long id;

    // 用户id
    private Long userId;

    // 商品id
    private Long goodsId;

    // 商品名称
    private String goodsName;

    // 秒杀活动价格
    private BigDecimal activityPrice;

    // 购买数量
    private Integer quantity;

    // 订单总金额
    private BigDecimal orderPrice;

    // 活动id
    private Long activityId;

    // 订单状态【1-已创建；2-已支付；0-已取消；-1-已删除】
    private Integer status;

    // 创建时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
