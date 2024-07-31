package com.young.seckill.order.application.event;

import com.young.seckill.common.response.RespResult;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.order.domain.event.SeckillOrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "event.publish.type", havingValue = "spring")
public class SeckillOrderEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillOrderEventListener.class);

    @EventListener
    public RespResult<String> execute(SeckillOrderEvent event) {
        LOGGER.info("Spring|OrderEvent 接收订单事件|{}", JACKSON.toJson(event));

        if (event.getOrderId() == null) {
            LOGGER.info("Spring|OrderEvent|订单参数错误");
        }
        return RespResult.success();
    }
}
