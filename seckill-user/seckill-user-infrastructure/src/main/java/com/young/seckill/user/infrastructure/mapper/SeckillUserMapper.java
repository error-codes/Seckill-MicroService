package com.young.seckill.user.infrastructure.mapper;

import com.young.seckill.user.domain.entity.SeckillUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SeckillUserMapper {

    /**
     * 保存一个用户信息
     */
    Integer saveSeckillUser(SeckillUser seckillUser);

    /**
     * 根据用户名获取用户信息
     */
    SeckillUser getSeckillUserByUsername(@Param("username") String username);

    /**
     * 根据手机号码获取用户信息
     */
    SeckillUser getSeckillUserByPhone(@Param("phone") String phone);

    /**
     * 根据用户ID获取用户信息
     */
    SeckillUser getSeckillUserById(@Param("userId") Long userId);
}
