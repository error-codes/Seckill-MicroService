package com.young.seckill.order.application.security;

public interface RiskControlService {

    /**
     * 对用户进行风控校验
     */
    boolean riskPolicy(Long userId);
}
