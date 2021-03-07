package com.zyq.rabbitmq.consumer.fanRabbit;

import com.zyq.rabbitmq.provider.ProviderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FanoutMessageConsumer {
    private Logger logger = LoggerFactory.getLogger(FanoutMessageConsumer.class);

    @RabbitListener(queues = {"first"+ ProviderProperties.FAN_QUEUE_NAME})
    public void FanoutMessageListener1(Map msg){
        logger.info("[FanoutMessageListener1][message received] {}", msg);
    }

    @RabbitListener(queues = {"second"+ProviderProperties.FAN_QUEUE_NAME})
    public void FanoutMessageListener2(Map msg){
        logger.info("[FanoutMessageListener2][message received] {}", msg);
    }
}
