package com.young.seckill.common.model.dto;

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
public class SeckillGoodsDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -6453314577262904212L;

    // 数据id
    private Long id;

    // 商品名称
    private String goodsName;

    // 秒杀活动id
    private Long activityId;

    // 活动开始时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    // 活动结束时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    // 商品原价
    private BigDecimal originalPrice;

    // 秒杀活动价
    private BigDecimal activityPrice;

    // 初始库存
    private Integer initialStock;

    // 限购个数
    private Integer limitNum;

    // 当前可用库存
    private Integer availableStock;

    // 商品描述
    private String description;

    // 商品图片
    private String imgUrl;

    // 秒杀状态【0-已发布；1-上线；-1-下线】
    private Integer status;

    // 缓存版本
    private Long version;
}
