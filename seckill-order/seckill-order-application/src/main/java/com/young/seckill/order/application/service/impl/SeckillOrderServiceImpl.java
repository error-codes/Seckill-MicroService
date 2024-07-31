package com.young.seckill.order.application.service.impl;

import com.young.seckill.common.cache.distribute.DistributedCacheService;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.exception.ExceptionChecker;
import com.young.seckill.common.exception.SeckillException;
import com.young.seckill.common.model.rocketmq.ExceptionTopicMessage;
import com.young.seckill.common.response.RespCode;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.order.application.model.command.SeckillOrderCommand;
import com.young.seckill.order.application.place.SeckillPlaceOrderService;
import com.young.seckill.order.application.security.RiskControlService;
import com.young.seckill.order.application.service.SeckillOrderService;
import com.young.seckill.order.domain.entity.SeckillOrder;
import com.young.seckill.order.domain.service.SeckillOrderDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillOrderServiceImpl.class);

    private final SeckillPlaceOrderService  seckillPlaceOrderService;
    private final SeckillOrderDomainService seckillOrderDomainService;
    private final DistributedCacheService   distributedCacheService;
    private final RiskControlService        riskControlService;


    @Lazy
    public SeckillOrderServiceImpl(SeckillPlaceOrderService seckillPlaceOrderService,
                                   SeckillOrderDomainService seckillOrderDomainService,
                                   DistributedCacheService distributedCacheService,
                                   RiskControlService riskControlService) {
        this.seckillPlaceOrderService = seckillPlaceOrderService;
        this.seckillOrderDomainService = seckillOrderDomainService;
        this.distributedCacheService = distributedCacheService;
        this.riskControlService = riskControlService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveSeckillOrder(Long userId, SeckillOrderCommand seckillOrderCommand) {
        ExceptionChecker.throwAssertIfNullOrEmpty(userId, RespCode.PARAMS_INVALID);
        ExceptionChecker.throwAssertIfNullOrEmpty(seckillOrderCommand, RespCode.PARAMS_INVALID);

        // 模拟风控
        if (!riskControlService.riskPolicy(userId)) {
            throw new SeckillException(RespCode.USER_ENV_RISK);
        }

        return seckillPlaceOrderService.placeOrder(userId, seckillOrderCommand);
    }

    @Override
    public List<SeckillOrder> getSeckillOrderByUserId(Long userId) {
        return seckillOrderDomainService.getSeckillOrderByUserId(userId);
    }

    @Override
    public List<SeckillOrder> getSeckillOrderByActivityId(Long activityId) {
        return seckillOrderDomainService.getSeckillOrderByActivityId(activityId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSeckillOrder(ExceptionTopicMessage exceptionTopicMessage) {
        // 成功提交过事务，才能清理订单，增加缓存库存
        boolean submit = distributedCacheService.hasKey(
                SeckillConstants.getKey(SeckillConstants.ORDER_TX_KEY, exceptionTopicMessage.getTxNo()));

        if (submit) {
            seckillOrderDomainService.deleteSeckillOrder(exceptionTopicMessage.getTxNo());
            handlerCacheStock(exceptionTopicMessage);
        } else {
            LOGGER.info("deleteOrder|订单微服务未执行本地事务|{}", exceptionTopicMessage.getTxNo());
        }
    }

    private void handlerCacheStock(ExceptionTopicMessage exceptionTopicMessage) {
        // 订单微服务之前未抛出异常，说明已经扣减了缓存中的库存，此时需要对缓存中的库存进行补偿
        if (!exceptionTopicMessage.getException()) {
            String luaKey = SeckillConstants.getKey(SeckillConstants.ORDER_TX_KEY, exceptionTopicMessage.getTxNo())
                                            .concat(SeckillConstants.LUA_SUFFIX);

            Long result = distributedCacheService.checkRecoverStockByLua(luaKey, SeckillConstants.TX_LOG_VALID_DURATION);

            // 已经执行过恢复缓存库存
            if (Objects.equals(result, SeckillConstants.CHECK_RECOVER_STOCK_HAS_EXECUTE)) {
                LOGGER.info("handlerCacheStock|已经执行过恢复缓存库存|{}", JACKSON.toJson(exceptionTopicMessage));
            } else {
                // 只有分布式锁和Lua脚本处理方式才会扣减缓存中的库存
                String key = SeckillConstants.getKey(SeckillConstants.GOODS_ITEM_STOCK_KEY_PREFIX, exceptionTopicMessage.getGoodsId());
                LOGGER.info("handlerCacheStock|回滚缓存库存|{}", JACKSON.toJson(exceptionTopicMessage));
                if (SeckillConstants.PLACE_ORDER_TYPE_LOCK.equalsIgnoreCase(exceptionTopicMessage.getOrderPlaceType())) {
                    distributedCacheService.increment(key, exceptionTopicMessage.getQuantity());
                } else if (SeckillConstants.PLACE_ORDER_TYPE_LUA.equalsIgnoreCase(exceptionTopicMessage.getOrderPlaceType())) {
                    distributedCacheService.incrementByLua(key, exceptionTopicMessage.getQuantity());
                }
            }
        }
    }
}
