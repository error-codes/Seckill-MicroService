package com.young.seckill.user.application.service;

import com.young.seckill.common.model.dto.SeckillUserDTO;
import com.young.seckill.user.domain.entity.SeckillUser;

public interface SeckillUserService {

    /**
     * 根据用户名获取用户信息
     */
    SeckillUser getSeckillUserByUsername(String username);

    /**
     * 根据手机号码获取用户信息
     */
    SeckillUser getSeckillUserByPhone(String phone);

    /**
     * 根据用户ID获取用户信息
     */
    SeckillUser getSeckillUserByUserId(Long userId);

    /**
     * 用户登录
     */
    String login(SeckillUserDTO seckillUserDTO);

    /**
     * 用户注册
     */
    void register(SeckillUserDTO seckillUserDTO);
}
