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
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(value = "order.place-type", havingValue = "db")
// SEATA 的 AT 分布式事务解决方案
public class SeckillPlaceOrderDBService implements SeckillPlaceOrderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeckillPlaceOrderDBService.class);

    private final RocketMQTemplate          rocketMQTemplate;
    private final DistributedCacheService   distributedCacheService;
    private final SeckillOrderDomainService seckillOrderDomainService;
    @DubboReference(version = "1.0.0")
    private       SeckillGoodsDubboService  seckillGoodsDubboService;

    public SeckillPlaceOrderDBService(
            RocketMQTemplate rocketMQTemplate,
            DistributedCacheService distributedCacheService,
            SeckillOrderDomainService seckillOrderDomainService) {

        this.rocketMQTemplate = rocketMQTemplate;
        this.distributedCacheService = distributedCacheService;
        this.seckillOrderDomainService = seckillOrderDomainService;
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public Long placeOrder(Long userId, SeckillOrderCommand seckillOrderCommand) {
        // 获取商品
        SeckillGoodsDTO seckillGoodsDTO =
                seckillGoodsDubboService.getSeckillGoods(seckillOrderCommand.getGoodsId(), seckillOrderCommand.getVersion());

        // 检测商品信息
        this.checkSeckillGoods(seckillOrderCommand, seckillGoodsDTO);

        // 生成事务ID
        long txNo = SnowFlakeFactory.getSnowFlakeIDCache().nextId();

        boolean exception = false;

        try {
            // 获取商品库存
            Integer availableStock = seckillGoodsDubboService.getAvailableStockByGoodsId(seckillOrderCommand.getGoodsId());

            // 库存不足
            ExceptionChecker.throwAssertIfLessThan(availableStock, seckillOrderCommand.getQuantity(), RespCode.STOCK_LT_ZERO);
        } catch (Exception e) {
            exception = true;
            LOGGER.error("SeckillPlaceOrderDbService|下单异常|参数: {}|异常信息: {}", JACKSON.toJson(seckillOrderCommand),
                         e.getMessage());
        }

        // 构建事务消息
        // 为了节省资源，此处的 txNo 即全局事务编号同时作为 OrderID 使用
        Message<String> message =
                this.getTxMessage(SeckillConstants.TX_MESSAGE_TOPIC, txNo, userId, SeckillConstants.PLACE_ORDER_TYPE_LUA, exception,
                                  seckillOrderCommand,
                                  seckillGoodsDTO);

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
            throw e;
        }
    }
}
