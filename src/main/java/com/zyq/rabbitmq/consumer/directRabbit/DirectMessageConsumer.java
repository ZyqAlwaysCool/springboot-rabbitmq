package com.zyq.rabbitmq.consumer.directRabbit;

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
public class DirectMessageConsumer {
    private Logger logger = LoggerFactory.getLogger(DirectMessageConsumer.class);
    @RabbitListener(queues = ProviderProperties.DIRECT_QUEUE_NAME)
    public void directListener(Map msg){
        logger.info("[directMessageListener][message received] {}", msg);
    }
}
