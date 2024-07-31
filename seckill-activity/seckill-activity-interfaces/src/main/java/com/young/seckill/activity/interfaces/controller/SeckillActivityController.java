package com.young.seckill.activity.interfaces.controller;

import com.young.seckill.activity.application.service.SeckillActivityService;
import com.young.seckill.activity.domain.entity.SeckillActivity;
import com.young.seckill.common.annotation.RequestDescDoc;
import com.young.seckill.common.model.dto.SeckillActivityDTO;
import com.young.seckill.common.response.RespResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/activity")
@RequestDescDoc("秒杀活动接口模块")
public class SeckillActivityController {

    private final SeckillActivityService seckillActivityService;

    public SeckillActivityController(SeckillActivityService seckillActivityService) {
        this.seckillActivityService = seckillActivityService;
    }

    @PostMapping
    @RequestDescDoc("创建秒杀活动")
    public RespResult<String> saveSeckillActivity(@RequestBody SeckillActivityDTO seckillActivityDTO) {
        seckillActivityService.saveSeckillActivity(seckillActivityDTO);
        return RespResult.success();
    }

    @GetMapping("/status")
    @RequestDescDoc("根据活动状态获取活动列表")
    public RespResult<List<SeckillActivity>> getSeckillActivityList(@RequestParam(required = false) Integer status) {
        return RespResult.success(seckillActivityService.getSeckillActivityList(status));
    }

    @GetMapping("/status_version")
    @RequestDescDoc("根据活动状态以及版本号获取活动列表")
    public RespResult<List<SeckillActivityDTO>> getSeckillActivityList(Long version, Integer status) {
        return RespResult.success(seckillActivityService.getSeckillActivityList(version, status));
    }

    @GetMapping("/status_time")
    @RequestDescDoc("根据活动状态以及时间获取活动列表")
    public RespResult<List<SeckillActivity>> getSeckillActivityListBetween(
            @RequestParam(required = false, defaultValue = "0") Integer status,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime currentTime) {
        return RespResult.success(seckillActivityService.getSeckillActivityListBetweenStartTimeAndEndTime(currentTime, status));
    }

    @GetMapping("/status_time_version")
    @RequestDescDoc("根据活动时间和状态以及版本号获取活动列表")
    public RespResult<List<SeckillActivityDTO>> getSeckillActivityListBetween(
            @RequestParam(required = false, defaultValue = "1") Long version,
            @RequestParam(required = false, defaultValue = "0") Integer status,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime currentTime) {
        return RespResult.success(
                seckillActivityService.getSeckillActivityListBetweenStartTimeAndEndTime(currentTime, status, version));
    }

    @GetMapping
    @RequestDescDoc("根据活动ID获取活动详情")
    public RespResult<SeckillActivity> getSeckillActivityById(Long activityId) {
        return RespResult.success(seckillActivityService.getSeckillActivityById(activityId));
    }

    @GetMapping("/version")
    @RequestDescDoc("根据活动ID以及版本号获取活动详情")
    public RespResult<SeckillActivityDTO> getSeckillActivityById(Long activityId, Long version) {
        return RespResult.success(seckillActivityService.getSeckillActivityById(activityId, version));
    }

    @PutMapping("/status")
    @RequestDescDoc("修改活动状态")
    public RespResult<String> updateSeckillActivityStatus(Long activityId, Integer status) {
        seckillActivityService.updateSeckillActivityStatus(activityId, status);
        return RespResult.success();
    }
}
