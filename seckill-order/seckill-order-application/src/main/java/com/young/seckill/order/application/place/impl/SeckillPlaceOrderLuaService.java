package com.young.seckill.order.application.place.impl;

import com.young.seckill.common.cache.distribute.DistributedCacheService;
import com.young.seckill.common.constant.SeckillConstants;
import com.young.seckill.common.exception.ExceptionChecker;
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
@ConditionalOnProperty(value = "order.place-type", havingValue = "lua")
// ROCKETMQ 的 事务消息 分布式解决方案
public class SeckillPlaceOrderLuaService implements SeckillPlaceOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillPlaceOrderLuaService.class);

    private final DistributedCacheService   distributedCacheService;
    private final SeckillOrderDomainService seckillOrderDomainService;
    private final RocketMQTemplate          rocketMQTemplate;

    @DubboReference(version = "1.0.0")
    private SeckillGoodsDubboService seckillGoodsDubboService;

    public SeckillPlaceOrderLuaService(SeckillOrderDomainService seckillOrderDomainService,
                                       DistributedCacheService distributedCacheService,
                                       RocketMQTemplate rocketMQTemplate) {
        this.distributedCacheService = distributedCacheService;
        this.seckillOrderDomainService = seckillOrderDomainService;
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public Long placeOrder(Long userId, SeckillOrderCommand seckillOrderCommand) {
        SeckillGoodsDTO seckillGoodsDTO =
                seckillGoodsDubboService.getSeckillGoods(seckillOrderCommand.getGoodsId(), seckillOrderCommand.getVersion());

        // 检测商品
        this.checkSeckillGoods(seckillOrderCommand, seckillGoodsDTO);

        boolean exception = false;
        Long txNo = SnowFlakeFactory.getSnowFlakeIDCache().nextId();

        String stockCache = SeckillConstants.getKey(SeckillConstants.GOODS_ITEM_STOCK_KEY_PREFIX, seckillOrderCommand.getGoodsId());

        try {
            // 获取商品限购信息
            Integer limit = distributedCacheService.getObject(
                    SeckillConstants.getKey(SeckillConstants.GOODS_ITEM_LIMIT_KEY_PREFIX, seckillGoodsDTO.getId()), Integer.class);

            // 如果从 Redis 获取到的限购信息为 null，则说明商品已经下线
            ExceptionChecker.throwAssertIfNullOrEmpty(limit, RespCode.GOODS_OFFLINE);
            ExceptionChecker.throwAssertIfLessThan(limit, seckillOrderCommand.getQuantity(), RespCode.BEYOND_LIMIT_NUM);

            Long result = distributedCacheService.decrementByLua(stockCache, seckillOrderCommand.getQuantity());

            distributedCacheService.checkResultByLua(result);
        } catch (Exception e) {
            LOGGER.error("SeckillPlaceOrderLuaService|下单异常|参数: {}|异常信息: {}", JACKSON.toJson(seckillOrderCommand),
                         e.getMessage());
            exception = true;
            // 内存缓存数据补偿
            distributedCacheService.incrementByLua(stockCache, seckillOrderCommand.getQuantity());
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
