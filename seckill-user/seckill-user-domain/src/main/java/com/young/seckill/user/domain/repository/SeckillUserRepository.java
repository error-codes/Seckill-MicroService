package com.young.seckill.user.domain.repository;

import com.young.seckill.user.domain.entity.SeckillUser;

public interface SeckillUserRepository {

    /**
     * 保存一个用户信息
     */
    Integer saveSeckillUser(SeckillUser seckillUser);

    /**
     * 根据用户名获取用户信息
     */
    SeckillUser getSeckillUserByUsername(String userName);

    /**
     * 根据手机号码获取用户信息
     */
    SeckillUser getSeckillUserByPhone(String phone);

    /**
     * 根据用户ID获取用户信息
     */
    SeckillUser getSeckillUserById(Long userId);

}
