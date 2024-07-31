package com.young.seckill.stock.interfaces.controller;

import com.young.seckill.common.annotation.RequestDescDoc;
import com.young.seckill.common.response.RespResult;
import com.young.seckill.stock.application.model.SeckillStockBucketDTO;
import com.young.seckill.stock.application.model.SeckillStockBucketWrapperCommand;
import com.young.seckill.stock.application.service.SeckillStockBucketService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stock")
public class SeckillStockController {

    private final SeckillStockBucketService seckillStockBucketService;

    public SeckillStockController(SeckillStockBucketService seckillStockBucketService) {
        this.seckillStockBucketService = seckillStockBucketService;
    }

    @PostMapping("/bucket")
    @RequestDescDoc("编排库存分桶")
    public RespResult<String> arrangeStockBuckets(@RequestAttribute Long userId,
                                                  @RequestBody SeckillStockBucketWrapperCommand seckillStockBucketWrapperCommand) {
        seckillStockBucketService.arrangeStockBuckets(userId, seckillStockBucketWrapperCommand);
        return RespResult.success();
    }

    @GetMapping("/bucket/total")
    @RequestDescDoc("获取库存分桶数据")
    public RespResult<SeckillStockBucketDTO> getTotalStockBuckets(Long goodsId, Long version) {
        return RespResult.success(seckillStockBucketService.getTotalStockBuckets(goodsId, version));
    }
}
