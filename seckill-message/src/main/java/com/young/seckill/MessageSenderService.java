package com.young.seckill;

import com.young.seckill.common.model.rocketmq.BasicTopicMessage;
import org.apache.rocketmq.client.producer.TransactionSendResult;

public interface MessageSenderService {

    /**
     * 发送消息
     *
     * @param message 普通消息
     */
    boolean sendMessage(BasicTopicMessage message);

    /**
     * 发送事务消息
     *
     * @param message 事务消息
     * @param args    附加参数
     */
    default TransactionSendResult sendTransactionMessage(BasicTopicMessage message, Object args) {
        return null;
    }
}
