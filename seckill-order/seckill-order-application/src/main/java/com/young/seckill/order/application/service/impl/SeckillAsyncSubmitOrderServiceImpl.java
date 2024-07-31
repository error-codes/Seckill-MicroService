package com.young.seckill.order.application.service.impl;

import com.young.seckill.common.cache.distribute.DistributedCacheService;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.exception.ExceptionChecker;
import com.young.seckill.common.model.dto.SeckillSubmitOrderDTO;
import com.young.seckill.common.response.RespCode;
import com.young.seckill.order.application.model.command.SeckillOrderCommand;
import com.young.seckill.order.application.model.task.SeckillOrderTask;
import com.young.seckill.order.application.place.SeckillPlaceOrderService;
import com.young.seckill.order.application.security.RiskControlService;
import com.young.seckill.order.application.service.PlaceOrderTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillAsyncSubmitOrderServiceImpl extends SeckillBaseSubmitOrderServiceImpl {

    private final PlaceOrderTaskService   placeOrderTaskService;
    private final DistributedCacheService distributedCacheService;

    public SeckillAsyncSubmitOrderServiceImpl(RiskControlService riskControlService,
                                              SeckillPlaceOrderService seckillPlaceOrderService,
                                              PlaceOrderTaskService placeOrderTaskService,
                                              DistributedCacheService distributedCacheService) {
        super(riskControlService, seckillPlaceOrderService);
        this.placeOrderTaskService = placeOrderTaskService;
        this.distributedCacheService = distributedCacheService;
    }

    @Override
    public SeckillSubmitOrderDTO saveSeckillOrder(Long userId, SeckillOrderCommand seckillOrderCommand) {
        // 进行基本参数检查
        checkSeckillOrder(userId, seckillOrderCommand);

        // 生成订单ID
        String orderTaskId =
                DigestUtils.md5DigestAsHex((userId + "_" + seckillOrderCommand.getGoodsId()).getBytes(StandardCharsets.UTF_8));

        // 构造下单任务
        SeckillOrderTask seckillOrderTask =
                new SeckillOrderTask(SeckillConstants.SUBMIT_ORDER_MESSAGE_TOPIC, orderTaskId, userId, seckillOrderCommand);

        // 提交订单
        boolean isSubmit = placeOrderTaskService.submitOrderTask(seckillOrderTask);

        // 提交失败
        ExceptionChecker.throwAssertIfFalse(isSubmit, RespCode.SAVE_ORDER_FAILED);

        return new SeckillSubmitOrderDTO(orderTaskId, seckillOrderCommand.getGoodsId(), SeckillConstants.TYPE_LICENSE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePlaceOrderTask(SeckillOrderTask seckillOrderTask) {
        Long orderId = seckillPlaceOrderService.placeOrder(seckillOrderTask.getUserId(), seckillOrderTask.getSeckillOrderCommand());
        if (orderId != null) {
            String key = SeckillConstants.getKey(SeckillConstants.ORDER_TASK_ORDER_KEY, seckillOrderTask.getOrderTaskId());
            distributedCacheService.put(key, orderId, SeckillConstants.ORDER_TASK_VALID_DURATION, TimeUnit.SECONDS);
        }
    }
}
