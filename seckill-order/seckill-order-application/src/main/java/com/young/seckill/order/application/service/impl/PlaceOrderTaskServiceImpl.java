package com.young.seckill.order.application.service.impl;

import com.young.seckill.MessageSenderService;
import com.young.seckill.common.cache.distribute.DistributedCacheService;
import com.young.seckill.common.cache.local.LocalCacheService;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.exception.ExceptionChecker;
import com.young.seckill.common.exception.SeckillException;
import com.young.seckill.common.lock.DistributedLock;
import com.young.seckill.common.lock.factory.DistributedLockFactory;
import com.young.seckill.common.response.RespCode;
import com.young.seckill.order.application.model.task.SeckillOrderTask;
import com.young.seckill.order.application.service.PlaceOrderTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PlaceOrderTaskServiceImpl implements PlaceOrderTaskService {

    private static final Logger                           LOGGER = LoggerFactory.getLogger(PlaceOrderTaskServiceImpl.class);
    private final        DistributedCacheService          distributedCacheService;
    private final        LocalCacheService<Long, Integer> localCacheService;
    private final        DistributedLockFactory           distributedLockFactory;
    private final        MessageSenderService             messageSenderService;
    private final        Lock                             lock   = new ReentrantLock();
    @Value("${submit.order.token.multiple: 1.5}")
    private              Double                           multiple;

    public PlaceOrderTaskServiceImpl(DistributedCacheService distributedCacheService,
                                     LocalCacheService<Long, Integer> localCacheService,
                                     DistributedLockFactory distributedLockFactory,
                                     MessageSenderService messageSenderService) {
        this.distributedCacheService = distributedCacheService;
        this.localCacheService = localCacheService;
        this.distributedLockFactory = distributedLockFactory;
        this.messageSenderService = messageSenderService;
    }

    @Override
    public boolean submitOrderTask(SeckillOrderTask seckillOrderTask) {
        ExceptionChecker.throwAssertIfNullOrEmpty(seckillOrderTask, RespCode.PARAMS_INVALID);
        String orderTaskKey = SeckillConstants.getKey(SeckillConstants.ORDER_TASK_KEY, seckillOrderTask.getOrderTaskId());
        // 检测是否恢复过库存
        Long result = distributedCacheService.checkRecoverStockByLua(orderTaskKey, SeckillConstants.ORDER_TASK_VALID_DURATION);

        // 已经执行过恢复库存
        ExceptionChecker.throwAssertIfEqual(result, SeckillConstants.CHECK_RECOVER_STOCK_HAS_EXECUTE, RespCode.REDUNDANT_SUBMIT);

        // 获取可用的下单许可证
        Long goodsId = seckillOrderTask.getSeckillOrderCommand().getGoodsId();
        Integer availableOrderToken = getAvailableOrderToken(goodsId);

        // 不存在下单许可
        ExceptionChecker.throwAssertIfZeroOrNegative(availableOrderToken, RespCode.ORDER_TOKEN_UNAVAILABLE);

        // 未获取到下单许可
        if (!takeOrderToken(goodsId)) {
            LOGGER.info("submitOrderTask|获取下单许可失败|{}, {}", seckillOrderTask.getUserId(), seckillOrderTask.getOrderTaskId());
            throw new SeckillException(RespCode.ORDER_TOKEN_UNAVAILABLE);
        }

        // 发送消息
        boolean isSent = messageSenderService.sendMessage(seckillOrderTask);
        if (!isSent) {
            LOGGER.info("submitOrderTask|下单任务提交失败|{}, {}", seckillOrderTask.getUserId(), seckillOrderTask.getOrderTaskId());
            // 恢复下单许可
            recoverOrderToken(goodsId);
            // 清除是否被执行过的数据
            distributedCacheService.delete(orderTaskKey);
        }
        return isSent;
    }

    /**
     * 获取下单许可，为何要循环三次处理业务
     */
    private boolean takeOrderToken(Long goodsId) {
        for (int i = 0; i < 3; i++) {
            Long result =
                    distributedCacheService.takeOrderToken(SeckillConstants.getKey(SeckillConstants.ORDER_TASK_TOKENS_KEY, goodsId));

            if (result == null) {
                return false;
            }

            if (Objects.equals(result, SeckillConstants.LUA_NOT_EXECUTED)) {
                refreshLocalAvailableOrderToken(goodsId);
                continue;
            }
            return Objects.equals(result, SeckillConstants.LUA_EXECUTED_SUCCESS);
        }
        return false;
    }

    /**
     * 恢复下单许可
     */
    private boolean recoverOrderToken(Long goodsId) {
        Long result = distributedCacheService.checkRecoverOrderToken(
                SeckillConstants.getKey(SeckillConstants.ORDER_TASK_TOKENS_KEY, goodsId));
        if (result == null) {
            return false;
        }
        if (result.equals(SeckillConstants.LUA_NOT_EXECUTED)) {
            refreshDistributedAvailableOrderToken(goodsId);
            return true;
        }
        return Objects.equals(result, SeckillConstants.LUA_EXECUTED_SUCCESS);
    }

    /**
     * 获取可用的下单许可
     */
    private Integer getAvailableOrderToken(Long goodsId) {
        Integer availableOrderToken = localCacheService.getIfPresent(goodsId);
        if (availableOrderToken != null) {
            return availableOrderToken;
        }
        return refreshLocalAvailableOrderToken(goodsId);
    }

    /**
     * 刷新本地缓存可用的下单许可，注意 DoubleCheckLock
     */
    private Integer refreshLocalAvailableOrderToken(Long goodsId) {
        Integer availableOrderToken = localCacheService.getIfPresent(goodsId);
        if (availableOrderToken != null) {
            return availableOrderToken;
        }
        String availableOrderTokenKey = SeckillConstants.getKey(SeckillConstants.ORDER_TASK_TOKENS_KEY, goodsId);
        Integer latestAvailableOrderToken = distributedCacheService.getObject(availableOrderTokenKey, Integer.class);
        if (latestAvailableOrderToken != null) {
            if (lock.tryLock()) {
                try {
                    localCacheService.put(goodsId, latestAvailableOrderToken);
                } finally {
                    lock.unlock();
                }
            }
            return latestAvailableOrderToken;
        }
        return refreshDistributedAvailableOrderToken(goodsId);
    }

    /**
     * 刷新分布式缓存的下单许可，注意 DoubleCheckLock
     */
    private Integer refreshDistributedAvailableOrderToken(Long goodsId) {
        String lockKey = SeckillConstants.getKey(SeckillConstants.ORDER_TASK_LOCK_REFRESH_TOKENS_KEY, goodsId);
        DistributedLock distributedLock = distributedLockFactory.getDistributedLock(lockKey);
        try {
            boolean isLocked = distributedLock.tryLock();
            if (!isLocked) {
                return null;
            }
            // 本地缓存已经更新
            Integer availableOrderToken = localCacheService.getIfPresent(goodsId);
            if (availableOrderToken != null) {
                return availableOrderToken;
            }
            // 获取分布式缓存数据
            String availableOrderTokenKey = SeckillConstants.getKey(SeckillConstants.ORDER_TASK_TOKENS_KEY, goodsId);
            Integer latestAvailableOrderToken = distributedCacheService.getObject(availableOrderTokenKey, Integer.class);
            if (latestAvailableOrderToken != null) {
                localCacheService.put(goodsId, latestAvailableOrderToken);
                return latestAvailableOrderToken;
            }
            // 本地缓存和分布式缓存均没数据，获取商品库存数据
            String goodsStockKey = SeckillConstants.getKey(SeckillConstants.GOODS_ITEM_STOCK_KEY_PREFIX, goodsId);
            Integer availableStock = distributedCacheService.getObject(goodsStockKey, Integer.class);
            if (availableStock == null || availableStock <= 0) {
                return null;
            }
            // 根据配置比例计算下单许可
            latestAvailableOrderToken = (int) Math.ceil(availableStock * multiple);
            distributedCacheService.put(availableOrderTokenKey, latestAvailableOrderToken, SeckillConstants.ORDER_TASK_VALID_DURATION,
                                        TimeUnit.SECONDS);
            localCacheService.put(goodsId, latestAvailableOrderToken);
            return latestAvailableOrderToken;
        } catch (Exception e) {
            LOGGER.error("refreshDistributedAvailableOrderToken|刷新下单许可失败: {}", goodsId, e);
        } finally {
            distributedLock.unlock();
        }
        return null;
    }
}
