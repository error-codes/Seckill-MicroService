package com.young.seckill.goods.interfaces.controller;

import com.young.seckill.common.annotation.RequestDescDoc;
import com.young.seckill.common.model.dto.SeckillGoodsDTO;
import com.young.seckill.common.response.RespResult;
import com.young.seckill.goods.application.command.SeckillGoodsCommand;
import com.young.seckill.goods.application.service.SeckillGoodsService;
import com.young.seckill.goods.domain.entity.SeckillGoods;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goods")
@RequestDescDoc("商品接口模块")
public class SeckillGoodsController {

    private final SeckillGoodsService seckillGoodsService;

    public SeckillGoodsController(SeckillGoodsService seckillGoodsService) {
        this.seckillGoodsService = seckillGoodsService;
    }

    @PostMapping
    @RequestDescDoc("保存秒杀商品")
    public RespResult<String> saveSeckillGoods(@RequestBody @NotNull SeckillGoodsCommand seckillGoodsCommand) {
        seckillGoodsService.saveSeckillGoods(seckillGoodsCommand);
        return RespResult.success();
    }

    @GetMapping
    @RequestDescDoc("根据商品ID获取商品详情")
    public RespResult<SeckillGoods> getSeckillGoodsByGoodsId(Long goodsId) {
        return RespResult.success(seckillGoodsService.getSeckillGoodsById(goodsId));
    }

    @GetMapping("/version")
    @RequestDescDoc("根据商品ID以及版本号获取商品详情")
    public RespResult<SeckillGoodsDTO> getSeckillGoodsByGoodsId(Long goodsId, Long version) {
        return RespResult.success(seckillGoodsService.getSeckillGoodsById(goodsId, version));
    }

    @GetMapping("/activity")
    @RequestDescDoc("根据活动ID获取商品列表")
    public RespResult<List<SeckillGoods>> getSeckillGoodsByActivityId(Long activityId) {
        return RespResult.success(seckillGoodsService.getSeckillGoodsByActivityId(activityId));
    }

    @GetMapping("/activity_version")
    @RequestDescDoc("根据活动ID以及版本号获取商品列表")
    public RespResult<List<SeckillGoodsDTO>> getSeckillGoodsByActivityId(Long activityId, Long version) {
        return RespResult.success(seckillGoodsService.getSeckillGoodsByActivityId(activityId, version));
    }

    @PutMapping("/status")
    @RequestDescDoc("更新商品状态")
    public RespResult<String> updateGoodsStatus(Long goodsId, Integer status) {
        seckillGoodsService.updateGoodsStatus(goodsId, status);
        return RespResult.success();
    }

}
