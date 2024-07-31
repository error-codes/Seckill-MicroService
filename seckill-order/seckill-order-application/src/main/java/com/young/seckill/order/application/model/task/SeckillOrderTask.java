package com.young.seckill.order.application.model.task;

import com.young.seckill.common.model.rocketmq.BasicTopicMessage;
import com.young.seckill.order.application.model.command.SeckillOrderCommand;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class SeckillOrderTask extends BasicTopicMessage {

    // 订单任务ID
    private String orderTaskId;

    // 用户ID
    private Long userId;

    // 订单数据
    private SeckillOrderCommand seckillOrderCommand;

    public SeckillOrderTask(String destination, String orderTaskId, Long userId, SeckillOrderCommand seckillOrderCommand) {
        super(destination);
        this.orderTaskId = orderTaskId;
        this.userId = userId;
        this.seckillOrderCommand = seckillOrderCommand;
    }
}
