package com.young.seckill.order.application.place.impl;

import com.young.seckill.common.cache.distribute.DistributedCacheService;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.exception.ExceptionChecker;
import com.young.seckill.common.exception.SeckillException;
import com.young.seckill.common.lock.DistributedLock;
import com.young.seckill.common.lock.factory.DistributedLockFactory;
import com.young.seckill.common.model.dto.SeckillGoodsDTO;
import com.young.seckill.common.model.rocketmq.TransactionTopicMessage;
import com.young.seckill.common.response.RespCode;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.common.utils.SnowFlakeFactory;
import com.young.seckill.dubbo.interfaces.goods.SeckillGoodsDubboService;
import com.young.seckill.order.application.model.command.SeckillOrderCommand;
import com.young.seckill.order.application.place.SeckillPlaceOrderService;
import com.young.seckill.order.domain.entity.SeckillOrder;
import com.young.seckill.order.domain.service.SeckillOrderDomainService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(value = "order.place-type", havingValue = "lock")
// SEATA 的 TCC 分布式事务解决方案
public class SeckillPlaceOrderLockService implements SeckillPlaceOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillPlaceOrderLockService.class);

    private final RocketMQTemplate          rocketMQTemplate;
    private final DistributedLockFactory    distributedLockFactory;
    private final DistributedCacheService   distributedCacheService;
    private final SeckillOrderDomainService seckillOrderDomainService;

    @DubboReference(version = "1.0.0")
    private SeckillGoodsDubboService seckillGoodsDubboService;

    public SeckillPlaceOrderLockService(RocketMQTemplate rocketMQTemplate,
                                        DistributedLockFactory distributedLockFactory,
                                        SeckillOrderDomainService seckillOrderDomainService,
                                        DistributedCacheService distributedCacheService) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.distributedLockFactory = distributedLockFactory;
        this.distributedCacheService = distributedCacheService;
        this.seckillOrderDomainService = seckillOrderDomainService;
    }

    @Override
    public Long placeOrder(Long userId, SeckillOrderCommand seckillOrderCommand) {
        // 获取商品信息
        SeckillGoodsDTO seckillGoodsDTO =
                seckillGoodsDubboService.getSeckillGoods(seckillOrderCommand.getGoodsId(), seckillOrderCommand.getVersion());

        // 检测商品信息
        this.checkSeckillGoods(seckillOrderCommand, seckillGoodsDTO);

        DistributedLock goodsLock = distributedLockFactory.getDistributedLock(
                SeckillConstants.getKey(SeckillConstants.DISTRIBUTED_ORDER_LOCK_SUFFIX, seckillOrderCommand.getGoodsId()));

        // 获取本地商品库存缓存
        String localKey = SeckillConstants.getKey(SeckillConstants.GOODS_ITEM_STOCK_KEY_PREFIX, seckillOrderCommand.getGoodsId());

        // 是否扣减库存缓存
        boolean isDecrementStockCache = false;
        boolean exception = false;

        // 获取全局事务ID
        long txNo = SnowFlakeFactory.getSnowFlakeIDCache().nextId();

        try {
            // 获取分布式锁失败
            if (!goodsLock.tryLock(2L, 5L, TimeUnit.SECONDS)) {
                throw new SeckillException(RespCode.RETRY_LATER);
            }

            // 查询库存信息
            Integer stock = distributedCacheService.getObject(localKey, Integer.class);

            // 库存不足
            ExceptionChecker.throwAssertIfLessThan(stock, seckillOrderCommand.getQuantity(), RespCode.STOCK_LT_ZERO);

            // 扣减库存
            Long result = distributedCacheService.decrement(localKey, seckillOrderCommand.getQuantity());
            if (result < 0) {
                throw new SeckillException(RespCode.STOCK_LT_ZERO);
            }

            // 正常执行扣减缓存中库存的操作
            isDecrementStockCache = true;

        } catch (Exception e) {
            // 扣减缓存后，发生异常，需要补偿缓存数据
            if (isDecrementStockCache) {
                distributedCacheService.increment(localKey, seckillOrderCommand.getQuantity());
            }
            if (e instanceof InterruptedException) {
                LOGGER.error("SeckillPlaceOrderLockService|下单时获取分布式锁失败|参数：{}|异常信息：{}", seckillOrderCommand,
                             e.getMessage());
            } else {
                LOGGER.error("SeckillPlaceOrderLockService|下单失败|参数：{}|异常信息：{}", seckillOrderCommand, e.getMessage());
            }
            exception = true;
        } finally {
            goodsLock.unlock();
        }

        // 构建事务消息
        // 为了节省资源，此处的 txNo 即全局事务编号同时作为 OrderID 使用
        Message<String> message =
                this.getTxMessage(SeckillConstants.TX_MESSAGE_TOPIC, txNo, userId, SeckillConstants.PLACE_ORDER_TYPE_LUA, exception,
                                  seckillOrderCommand, seckillGoodsDTO);

        // 发送事务消息
        rocketMQTemplate.sendMessageInTransaction(SeckillConstants.TX_MESSAGE_TOPIC, message, null);
        return txNo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrderInTransaction(TransactionTopicMessage transactionTopicMessage) {
        try {
            Boolean submit = distributedCacheService.hasKey(
                    SeckillConstants.getKey(SeckillConstants.ORDER_TX_KEY, transactionTopicMessage.getTxNo()));
            if (submit) {
                LOGGER.info("saveOrderInTransaction|已经执行过本地事务|{}", transactionTopicMessage.getTxNo());
            } else {
                // 构建订单
                SeckillOrder seckillOrder = this.buildSeckillOrder(transactionTopicMessage);
                // 保存订单
                seckillOrderDomainService.saveSeckillOrder(seckillOrder);
                // 保存事务日志
                distributedCacheService.put(SeckillConstants.getKey(SeckillConstants.ORDER_TX_KEY, transactionTopicMessage.getTxNo()),
                                            transactionTopicMessage.getTxNo(), SeckillConstants.TX_LOG_VALID_DURATION, TimeUnit.DAYS);

            }
        } catch (Exception e) {
            LOGGER.error("saveOrderInTransaction|执行本地事务异常|{}", e.getMessage());
            distributedCacheService.delete(SeckillConstants.getKey(SeckillConstants.ORDER_TX_KEY, transactionTopicMessage.getTxNo()));
            this.rollbackCacheStack(transactionTopicMessage);
            throw e;
        }
    }

    private void rollbackCacheStack(TransactionTopicMessage transactionTopicMessage) {
        // 已扣减缓存库存
        if (!transactionTopicMessage.getException()) {
            String luaKey = SeckillConstants.getKey(SeckillConstants.ORDER_TX_KEY, transactionTopicMessage.getTxNo())
                                            .concat(SeckillConstants.LUA_SUFFIX);
            Long result = distributedCacheService.checkRecoverStockByLua(luaKey, SeckillConstants.TX_LOG_VALID_DURATION);
            // 已经执行过恢复缓存库存的方法
            if (Objects.equals(result, SeckillConstants.CHECK_RECOVER_STOCK_HAS_EXECUTE)) {
                LOGGER.info("handlerCacheStock|已经执行过恢复缓存库存|{}", JACKSON.toJson(transactionTopicMessage));
            } else {
                // 只有分布式锁方式和Lua脚本方法才会扣减缓存中库存
                String key =
                        SeckillConstants.getKey(SeckillConstants.GOODS_ITEM_STOCK_KEY_PREFIX, transactionTopicMessage.getGoodsId());
                distributedCacheService.increment(key, transactionTopicMessage.getQuantity());
            }
        }
    }
}
