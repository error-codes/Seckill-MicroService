package com.young.seckill.order.application.place;

import com.young.seckill.common.exception.ExceptionChecker;
import com.young.seckill.common.model.dto.SeckillGoodsDTO;
import com.young.seckill.common.model.enums.SeckillGoodsStatus;
import com.young.seckill.common.model.enums.SeckillOrderStatus;
import com.young.seckill.common.model.rocketmq.TransactionTopicMessage;
import com.young.seckill.common.response.RespCode;
import com.young.seckill.common.utils.JACKSON;
import com.young.seckill.order.application.model.command.SeckillOrderCommand;
import com.young.seckill.order.domain.entity.SeckillOrder;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public interface SeckillPlaceOrderService {

    /**
     * 处理订单
     *
     * @param userId              用户ID
     * @param seckillOrderCommand 订单信息
     */
    Long placeOrder(Long userId, SeckillOrderCommand seckillOrderCommand);

    /**
     * 保存订单异步事务
     *
     * @param transactionTopicMessage 事务消息
     */
    void saveOrderInTransaction(TransactionTopicMessage transactionTopicMessage);

    /**
     * 构建订单信息
     *
     * @param orderId             订单ID
     * @param userId              用户ID
     * @param seckillOrderCommand 订单信息
     * @param seckillGoodsDTO     商品信息
     */
    default SeckillOrder buildSeckillOrder(Long orderId,
                                           Long userId,
                                           SeckillOrderCommand seckillOrderCommand,
                                           SeckillGoodsDTO seckillGoodsDTO) {
        SeckillOrder seckillOrder = new SeckillOrder();
        BeanUtils.copyProperties(seckillOrderCommand, seckillOrder);
        seckillOrder.setId(orderId);
        seckillOrder.setUserId(userId);
        seckillOrder.setCreateTime(LocalDateTime.now());
        seckillOrder.setGoodsName(seckillGoodsDTO.getGoodsName());
        seckillOrder.setStatus(SeckillOrderStatus.CREATED.getCode());
        seckillOrder.setActivityPrice(seckillGoodsDTO.getActivityPrice());
        seckillOrder.setOrderPrice(seckillGoodsDTO.getActivityPrice().multiply(BigDecimal.valueOf(seckillOrder.getQuantity())));

        return seckillOrder;
    }

    /**
     * 通过事务消息构建订单信息
     *
     * @param transactionTopicMessage 事务消息
     */
    default SeckillOrder buildSeckillOrder(TransactionTopicMessage transactionTopicMessage) {
        return new SeckillOrder(transactionTopicMessage.getTxNo(), transactionTopicMessage.getUserId(),
                                transactionTopicMessage.getGoodsId(), transactionTopicMessage.getGoodsName(),
                                transactionTopicMessage.getActivityPrice(), transactionTopicMessage.getQuantity(),
                                transactionTopicMessage.getActivityPrice()
                                                       .multiply(BigDecimal.valueOf(transactionTopicMessage.getQuantity())),
                                transactionTopicMessage.getActivityId(), SeckillOrderStatus.CREATED.getCode(), LocalDateTime.now());
    }

    /**
     * 构建事务消息
     *
     * @param destination         目的地
     * @param txNo                事务编号
     * @param userId              用户ID
     * @param placeOrderType      订单处理方式
     * @param exception           是否异常
     * @param seckillOrderCommand 订单信息
     * @param seckillGoodsDTO     商品信息
     */
    default Message<String> getTxMessage(String destination,
                                         Long txNo,
                                         Long userId,
                                         String placeOrderType,
                                         Boolean exception,
                                         SeckillOrderCommand seckillOrderCommand,
                                         SeckillGoodsDTO seckillGoodsDTO) {
        // 构建事务消息
        TransactionTopicMessage transactionTopicMessage =
                new TransactionTopicMessage(destination, txNo, seckillGoodsDTO.getId(), seckillOrderCommand.getQuantity(),
                                            placeOrderType, exception, seckillGoodsDTO.getActivityId(),
                                            seckillOrderCommand.getVersion(), userId, seckillGoodsDTO.getGoodsName(),
                                            seckillGoodsDTO.getActivityPrice());

        return MessageBuilder.withPayload(Objects.requireNonNull(JACKSON.toJson(transactionTopicMessage))).build();
    }

    /**
     * 检验对象参数
     *
     * @param seckillOrderCommand 订单信息
     * @param seckillGoodsDTO     商品信息
     */
    default void checkSeckillGoods(SeckillOrderCommand seckillOrderCommand, SeckillGoodsDTO seckillGoodsDTO) {
        // 商品不存在
        ExceptionChecker.throwAssertIfNullOrEmpty(seckillGoodsDTO, RespCode.GOODS_NOT_EXISTS);

        // 商品未上线
        ExceptionChecker.throwAssertIfEqual(seckillGoodsDTO.getStatus(), SeckillGoodsStatus.PUBLISHED.getCode(),
                                            RespCode.GOODS_UN_PUBLISH);

        // 商品已下架
        ExceptionChecker.throwAssertIfEqual(seckillGoodsDTO.getStatus(), SeckillGoodsStatus.OFFLINE.getCode(),
                                            RespCode.GOODS_NOT_EXISTS);

        // 商品限购
        ExceptionChecker.throwAssertIfLessThan(seckillGoodsDTO.getLimitNum(), seckillOrderCommand.getQuantity(),
                                               RespCode.BEYOND_LIMIT_NUM);

        // 商品库存不足
        ExceptionChecker.throwAssertIfNullOrEmpty(seckillGoodsDTO.getAvailableStock(), RespCode.STOCK_LT_ZERO);
        ExceptionChecker.throwAssertIfZeroOrNegative(seckillGoodsDTO.getAvailableStock(), RespCode.GOODS_NOT_EXISTS);
        ExceptionChecker.throwAssertIfLessThan(seckillGoodsDTO.getAvailableStock(), seckillOrderCommand.getQuantity(),
                                               RespCode.STOCK_LT_ZERO);
    }

    /**
     * TCC提交方法【confirm】
     */
    default Long confirm(BusinessActionContext businessActionContext) {
        return 0L;
    }


    /**
     * TCC回滚方法【cancel】
     */
    default Long cancel(BusinessActionContext businessActionContext) {
        return 0L;
    }
}
