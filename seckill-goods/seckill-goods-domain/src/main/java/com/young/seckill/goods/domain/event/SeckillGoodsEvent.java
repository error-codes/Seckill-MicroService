package com.young.seckill.goods.domain.event;

import com.young.seckill.common.model.event.SeckillEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class SeckillGoodsEvent extends SeckillEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = -4267608291309890294L;

    // 商品ID
    private Long goodsId;

    // 商品状态
    private Integer status;

    // 活动ID
    private Long activityId;

    public SeckillGoodsEvent(String eventName, Long goodsId, Long activityId, Integer status, String destination) {
        super(eventName, LocalDateTime.now(), destination);
        this.goodsId = goodsId;
        this.status = status;
        this.activityId = activityId;
    }
}
