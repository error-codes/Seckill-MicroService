package com.young.seckill.stock.domain.event;

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
public class SeckillStockBucketEvent extends SeckillEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = -7861220083405291475L;

    // 商品ID
    private Long goodsId;

    // 事件状态
    private Integer status;

    public SeckillStockBucketEvent(String eventName, Long goodsId, Integer status, String destination) {
        super(eventName, LocalDateTime.now(), destination);
        this.goodsId = goodsId;
        this.status = status;
    }

}
