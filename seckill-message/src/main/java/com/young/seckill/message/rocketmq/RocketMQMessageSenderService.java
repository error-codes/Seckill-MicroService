package com.young.seckill.message.rocketmq;

import com.young.seckill.MessageSenderService;
import com.young.seckill.common.model.rocketmq.BasicTopicMessage;
import com.young.seckill.common.utils.JACKSON;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@ConditionalOnProperty(name = "message.send.type", havingValue = "rocketmq")
public class RocketMQMessageSenderService implements MessageSenderService {

    private final RocketMQTemplate rocketMQTemplate;

    public RocketMQMessageSenderService(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public boolean sendMessage(BasicTopicMessage message) {
        try {
            SendResult sendResult = rocketMQTemplate.syncSend(message.getDestination(), getMessage(message));
            return SendStatus.SEND_OK.equals(sendResult.getSendStatus());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public TransactionSendResult sendTransactionMessage(BasicTopicMessage message, Object args) {
        return rocketMQTemplate.sendMessageInTransaction(message.getDestination(), getMessage(message), args);
    }

    private Message<String> getMessage(BasicTopicMessage message) {
        return MessageBuilder.withPayload(Objects.requireNonNull(JACKSON.toJson(message))).build();
    }
}
