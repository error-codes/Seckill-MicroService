package com.young.seckill.order.domain.service.impl;

import com.young.seckill.MessageSenderService;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.exception.ExceptionChecker;
import com.young.seckill.common.model.enums.SeckillOrderStatus;
import com.young.seckill.common.response.RespCode;
import com.young.seckill.order.domain.entity.SeckillOrder;
import com.young.seckill.order.domain.event.SeckillOrderEvent;
import com.young.seckill.order.domain.repository.SeckillOrderRepository;
import com.young.seckill.order.domain.service.SeckillOrderDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeckillOrderDomainServiceImpl implements SeckillOrderDomainService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillOrderDomainServiceImpl.class);

    private final SeckillOrderRepository seckillOrderRepository;
    private final MessageSenderService   messageSenderService;


    public SeckillOrderDomainServiceImpl(SeckillOrderRepository seckillOrderRepository, MessageSenderService messageSenderService) {
        this.seckillOrderRepository = seckillOrderRepository;
        this.messageSenderService = messageSenderService;
    }

    @Override
    public boolean saveSeckillOrder(SeckillOrder seckillOrder) {
        String eventName = "ORDER_PUBLISH";
        LOGGER.info("{}|用户正在下单|{}", eventName, seckillOrder);

        ExceptionChecker.throwAssertIfNullOrEmpty(seckillOrder, RespCode.PARAMS_INVALID);

        seckillOrder.setStatus(SeckillOrderStatus.CREATED.getCode());
        boolean saved = seckillOrderRepository.saveSeckillOrder(seckillOrder);
        if (saved) {
            LOGGER.info("{}|创建订单成功|{}", eventName, seckillOrder);
            SeckillOrderEvent seckillOrderEvent =
                    new SeckillOrderEvent(eventName, seckillOrder.getId(), SeckillOrderStatus.CREATED.getCode(),
                                          SeckillConstants.EVENT_TOPIC_ORDER_KEY);
            messageSenderService.sendMessage(seckillOrderEvent);
        }

        return saved;
    }

    @Override
    public List<SeckillOrder> getSeckillOrderByUserId(Long userId) {
        ExceptionChecker.throwAssertIfNullOrEmpty(userId, RespCode.PARAMS_INVALID);
        return seckillOrderRepository.getSeckillOrderByUserId(userId);
    }

    @Override
    public List<SeckillOrder> getSeckillOrderByActivityId(Long activityId) {
        ExceptionChecker.throwAssertIfNullOrEmpty(activityId, RespCode.PARAMS_INVALID);
        return seckillOrderRepository.getSeckillOrderByActivityId(activityId);
    }

    @Override
    public void deleteSeckillOrder(Long orderId) {
        String eventName = "ORDER_DELETE";
        ExceptionChecker.throwAssertIfNullOrEmpty(orderId, RespCode.PARAMS_INVALID);
        if (seckillOrderRepository.deleteSeckillOrder(orderId)) {
            LOGGER.info("deleteSeckillOrder|删除订单成功|{}", orderId);
            SeckillOrderEvent seckillOrderEvent = new SeckillOrderEvent(eventName, orderId, SeckillOrderStatus.DELETED.getCode(),
                                                                        SeckillConstants.EVENT_TOPIC_ORDER_KEY);
            messageSenderService.sendMessage(seckillOrderEvent);
        }
    }
}
