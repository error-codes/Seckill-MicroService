package com.young.seckill.order.application.service.impl;

import com.young.seckill.common.exception.ExceptionChecker;
import com.young.seckill.common.exception.SeckillException;
import com.young.seckill.common.model.dto.SeckillGoodsDTO;
import com.young.seckill.common.response.RespCode;
import com.young.seckill.dubbo.interfaces.goods.SeckillGoodsDubboService;
import com.young.seckill.order.application.model.command.SeckillOrderCommand;
import com.young.seckill.order.application.place.SeckillPlaceOrderService;
import com.young.seckill.order.application.security.RiskControlService;
import com.young.seckill.order.application.service.SeckillSubmitOrderService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
public abstract class SeckillBaseSubmitOrderServiceImpl implements SeckillSubmitOrderService {

    protected final SeckillPlaceOrderService seckillPlaceOrderService;
    private final   RiskControlService       riskControlService;
    @DubboReference(version = "1.0.0")
    private         SeckillGoodsDubboService seckillGoodsDubboService;

    protected SeckillBaseSubmitOrderServiceImpl(RiskControlService riskControlService,
                                                SeckillPlaceOrderService seckillPlaceOrderService) {
        this.riskControlService = riskControlService;
        this.seckillPlaceOrderService = seckillPlaceOrderService;
    }

    @Override
    public void checkSeckillOrder(Long userId, SeckillOrderCommand seckillOrderCommand) {
        ExceptionChecker.throwAssertIfNullOrEmpty(userId, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(seckillOrderCommand, RespCode.PARAMS_INVALID);

        // 模拟风控
        if (riskControlService.riskPolicy(userId)) {
            throw new SeckillException(RespCode.USER_ENV_RISK);
        }

        // 获取商品信息
        SeckillGoodsDTO seckillGoodsDTO =
                seckillGoodsDubboService.getSeckillGoods(seckillOrderCommand.getGoodsId(), seckillOrderCommand.getVersion());

        // 检测商品信息
        seckillPlaceOrderService.checkSeckillGoods(seckillOrderCommand, seckillGoodsDTO);
    }
}
