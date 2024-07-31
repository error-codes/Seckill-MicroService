package com.young.seckill.message.spring;

import com.young.seckill.MessageSenderService;
import com.young.seckill.common.model.rocketmq.BasicTopicMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "message.send.type", havingValue = "spring")
public class SpringMessageSenderService implements MessageSenderService {

    private final ApplicationEventPublisher publisher;

    public SpringMessageSenderService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public boolean sendMessage(BasicTopicMessage message) {
        try {
            publisher.publishEvent(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
