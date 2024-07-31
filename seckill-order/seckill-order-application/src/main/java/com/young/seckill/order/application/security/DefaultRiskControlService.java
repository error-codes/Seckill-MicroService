package com.young.seckill.order.application.security;

import org.springframework.stereotype.Service;

@Service
public class DefaultRiskControlService implements RiskControlService {

    @Override
    public boolean riskPolicy(Long userId) {
        return true;
    }
}
