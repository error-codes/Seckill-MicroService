package com.young.seckill.order.domain.event;

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
public class SeckillOrderEvent extends SeckillEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 555961143963877972L;

    // 订单ID
    private Long orderId;

    // 订单状态
    private Integer status;

    public SeckillOrderEvent(String eventName, Long orderId, Integer status, String destination) {
        super(eventName, LocalDateTime.now(), destination);
        this.orderId = orderId;
        this.status = status;
    }
}
