package com.young.seckill.order.interfaces.controller;

import com.young.seckill.common.annotation.RequestDescDoc;
import com.young.seckill.common.response.RespResult;
import com.young.seckill.order.application.model.command.SeckillOrderCommand;
import com.young.seckill.order.application.service.SeckillOrderService;
import com.young.seckill.order.domain.entity.SeckillOrder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@RequestDescDoc("订单接口模块")
public class SeckillOrderController {

    private final SeckillOrderService seckillOrderService;


    public SeckillOrderController(SeckillOrderService seckillOrderService) {
        this.seckillOrderService = seckillOrderService;
    }

    @PostMapping
    @RequestDescDoc("保存订单信息")
    public RespResult<Long> saveSeckillOrder(@RequestAttribute Long userId, SeckillOrderCommand seckillOrderCommand) {
        return RespResult.success(seckillOrderService.saveSeckillOrder(userId, seckillOrderCommand));
    }

    @GetMapping("/user")
    @RequestDescDoc("获取用户订单列表")
    public RespResult<List<SeckillOrder>> getSeckillOrderByUserId(Long userId) {
        return RespResult.success(seckillOrderService.getSeckillOrderByUserId(userId));
    }

    @GetMapping("/activity")
    @RequestDescDoc("获取活动订单列表")
    public RespResult<List<SeckillOrder>> getSeckillOrderByActivityId(Long activityId) {
        return RespResult.success(seckillOrderService.getSeckillOrderByActivityId(activityId));
    }
}
