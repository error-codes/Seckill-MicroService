package com.young.seckill.user.interfaces.controller;

import com.young.seckill.common.annotation.RequestDescDoc;
import com.young.seckill.common.model.dto.SeckillUserDTO;
import com.young.seckill.common.response.RespResult;
import com.young.seckill.user.application.service.SeckillUserService;
import com.young.seckill.user.domain.entity.SeckillUser;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequestDescDoc("用户接口模块")
public class SeckillUserController {

    private final SeckillUserService seckillUserService;

    public SeckillUserController(SeckillUserService seckillUserService) {
        this.seckillUserService = seckillUserService;
    }

    @GetMapping
    @RequestDescDoc("获取用户详情")
    public RespResult<SeckillUser> getUserByUsername(String username) {
        return RespResult.success(seckillUserService.getSeckillUserByUsername(username));
    }

    @PostMapping("/login")
    @RequestDescDoc("用户登录")
    public RespResult<String> login(@RequestBody SeckillUserDTO seckillUserDTO) {
        return RespResult.success(seckillUserService.login(seckillUserDTO));
    }

    @PutMapping("/register")
    @RequestDescDoc("用户注册")
    public RespResult<String> register(@RequestBody SeckillUserDTO seckillUserDTO) {
        seckillUserService.register(seckillUserDTO);
        return RespResult.success();
    }
}
