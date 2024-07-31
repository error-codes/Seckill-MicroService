package com.young.seckill.order.application.service.impl;

import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.model.dto.SeckillSubmitOrderDTO;
import com.young.seckill.order.application.model.command.SeckillOrderCommand;
import com.young.seckill.order.application.place.SeckillPlaceOrderService;
import com.young.seckill.order.application.security.RiskControlService;
import com.young.seckill.order.application.service.SeckillSubmitOrderService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "submit.order.type", havingValue = "sync")
public class SeckillSyncSubmitOrderServiceImpl extends SeckillBaseSubmitOrderServiceImpl implements SeckillSubmitOrderService {

    protected SeckillSyncSubmitOrderServiceImpl(RiskControlService riskControlService,
                                                SeckillPlaceOrderService seckillPlaceOrderService) {
        super(riskControlService, seckillPlaceOrderService);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SeckillSubmitOrderDTO saveSeckillOrder(Long userId, SeckillOrderCommand seckillOrderCommand) {
        // 检查订单参数
        checkSeckillOrder(userId, seckillOrderCommand);
        return new SeckillSubmitOrderDTO(String.valueOf(seckillPlaceOrderService.placeOrder(userId, seckillOrderCommand)),
                                         seckillOrderCommand.getGoodsId(), SeckillConstants.TYPE_ORDER);
    }
}
