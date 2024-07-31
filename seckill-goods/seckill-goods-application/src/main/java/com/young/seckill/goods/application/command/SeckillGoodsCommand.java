package com.young.seckill.goods.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillGoodsCommand implements Serializable {

    @Serial
    private static final long serialVersionUID = -3145840760255659822L;

    // 商品名称
    private String goodsName;

    // 活动ID
    private Long activityId;

    // 商品原价
    private BigDecimal originalPrice;

    // 秒杀价格
    private BigDecimal activityPrice;

    // 初始库存
    private Integer initialStock;

    // 限购个数
    private Integer limitNum;

    // 描述
    private String description;

    // 图片
    private String imgUrl;

    // 版本号，默认为 1
    private Long version = 1L;
}
