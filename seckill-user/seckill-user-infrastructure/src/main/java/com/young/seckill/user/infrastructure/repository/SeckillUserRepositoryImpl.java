package com.young.seckill.user.infrastructure.repository;

import com.young.seckill.user.domain.entity.SeckillUser;
import com.young.seckill.user.domain.repository.SeckillUserRepository;
import com.young.seckill.user.infrastructure.mapper.SeckillUserMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SeckillUserRepositoryImpl implements SeckillUserRepository {

    private final SeckillUserMapper seckillUserMapper;

    public SeckillUserRepositoryImpl(SeckillUserMapper seckillUserMapper) {
        this.seckillUserMapper = seckillUserMapper;
    }

    @Override
    public Integer saveSeckillUser(SeckillUser seckillUser) {
        return seckillUserMapper.saveSeckillUser(seckillUser);
    }

    @Override
    public SeckillUser getSeckillUserByUsername(String username) {
        return seckillUserMapper.getSeckillUserByUsername(username);
    }

    @Override
    public SeckillUser getSeckillUserByPhone(String phone) {
        return seckillUserMapper.getSeckillUserByPhone(phone);
    }

    @Override
    public SeckillUser getSeckillUserById(Long userId) {
        return seckillUserMapper.getSeckillUserById(userId);
    }
}
