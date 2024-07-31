package com.young.seckill.activity.domain.event;

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
public class SeckillActivityEvent extends SeckillEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = -813686092756779410L;

    // 活动ID
    private Long activityId;

    // 活动状态
    private Integer status;

    public SeckillActivityEvent(String eventName, Long activityId, Integer status, String destination) {
        super(eventName, LocalDateTime.now(), destination);
        this.activityId = activityId;
        this.status = status;
    }

}