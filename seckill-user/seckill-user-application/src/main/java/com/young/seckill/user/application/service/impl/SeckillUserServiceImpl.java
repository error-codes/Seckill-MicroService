package com.young.seckill.user.application.service.impl;

import com.young.seckill.common.cache.distribute.DistributedCacheService;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.exception.ExceptionChecker;
import com.young.seckill.common.model.dto.SeckillUserDTO;
import com.young.seckill.common.response.RespCode;
import com.young.seckill.common.utils.JwtProvider;
import com.young.seckill.common.utils.SnowFlakeFactory;
import com.young.seckill.user.application.service.SeckillUserService;
import com.young.seckill.user.domain.entity.SeckillUser;
import com.young.seckill.user.domain.repository.SeckillUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillUserServiceImpl implements SeckillUserService {

    private final PasswordEncoder         passwordEncoder;
    private final SeckillUserRepository   seckillUserRepository;
    private final DistributedCacheService distributedCacheService;

    public SeckillUserServiceImpl(
            PasswordEncoder passwordEncoder,
            SeckillUserRepository seckillUserRepository,
            DistributedCacheService distributedCacheService) {
        this.passwordEncoder = passwordEncoder;
        this.seckillUserRepository = seckillUserRepository;
        this.distributedCacheService = distributedCacheService;
    }

    @Override
    public SeckillUser getSeckillUserByUsername(String username) {
        return seckillUserRepository.getSeckillUserByUsername(username);
    }

    @Override
    public SeckillUser getSeckillUserByPhone(String phone) {
        return seckillUserRepository.getSeckillUserByPhone(phone);
    }

    @Override
    public SeckillUser getSeckillUserByUserId(Long userId) {
        return seckillUserRepository.getSeckillUserById(userId);
    }

    @Override
    public String login(SeckillUserDTO seckillUserDTO) {
        SeckillUser seckillUser = getSeckillUserByUsername(seckillUserDTO.getUsername());

        ExceptionChecker.throwAssertIfNullOrEmpty(seckillUser, RespCode.USERNAME_IS_ERROR);

        ExceptionChecker.throwAssertIfFalse(passwordEncoder.matches(seckillUserDTO.getPassword(), seckillUser.getPassword()),
                                            RespCode.PASSWORD_IS_ERROR);

        String token = JwtProvider.generateToken(seckillUser.getId());
        String key = SeckillConstants.getKey(SeckillConstants.USER_KEY, String.valueOf(seckillUser.getId()));
        distributedCacheService.put(key, seckillUser, SeckillConstants.TOKEN_VALID_DURATION, TimeUnit.DAYS);
        return token;
    }

    @Override
    public void register(SeckillUserDTO seckillUserDTO) {
        SeckillUser seckillUser = getSeckillUserByPhone(seckillUserDTO.getPhone());
        if (seckillUser == null || !Objects.equals(seckillUser.getPhone(), seckillUserDTO.getPhone())) {
            seckillUser = new SeckillUser();
            seckillUser.setId(SnowFlakeFactory.getSnowFlakeIDCache().nextId());
            seckillUser.setUsername(seckillUserDTO.getUsername());
            seckillUser.setPassword(passwordEncoder.encode(seckillUserDTO.getPassword()));
            seckillUser.setPhone(seckillUserDTO.getPhone());
            seckillUser.setStatus(1);
            seckillUser.setCreateTime(LocalDateTime.now());
            seckillUserRepository.saveSeckillUser(seckillUser);
        }
    }
}
