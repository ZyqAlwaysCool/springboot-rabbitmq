package com.zyq.rabbitmq.consumer.topicRabbit;

import com.zyq.rabbitmq.provider.ProviderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @RabbitListener 和 @RabbitHandler 搭配使用
 * @RabbitListener 可以标注在类上面，需配合 @RabbitHandler 注解一起使用
 * @RabbitListener 标注在类上面表示当有收到消息的时候，就交给 @RabbitHandler 的方法处理，
 * 具体使用哪个方法处理，根据 MessageConverter 转换后的参数类型
 *
 * @RabbitListener也可以单独标注在方法上使用
 */
@Component
@RabbitListener(queues = {ProviderProperties.TOPIC_QUEUE_NAME+"One", ProviderProperties.TOPIC_QUEUE_NAME+"Two"})
public class TopicMessageConsumer {
    private Logger logger = LoggerFactory.getLogger(TopicMessageConsumer.class);
    @RabbitHandler
    public void topicMessageListener(Map msg){
        logger.info("[topicMessageListener][message recieved] {}", msg);
    }
}
