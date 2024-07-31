package com.young.seckill.common.model.rocketmq;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BasicTopicMessage {

    /**
     * 消息目的地
     */
    private String destination;

    public BasicTopicMessage(String destination) {
        this.destination = destination;
    }
}
