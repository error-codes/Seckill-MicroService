package com.young.seckill.common.model.event;

import com.young.seckill.common.model.rocketmq.BasicTopicMessage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class SeckillEvent extends BasicTopicMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 7750783378422212486L;

    // 事件名称
    private String eventName;

    // 触发时间
    private LocalDateTime releaseTime;

    public SeckillEvent(String eventName, LocalDateTime releaseTime, String destination) {
        super(destination);
        this.eventName = eventName;
        this.releaseTime = releaseTime;
    }
}
