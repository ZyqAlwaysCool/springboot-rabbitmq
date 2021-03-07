package com.zyq.rabbitmq.configuration.consumer;

import com.zyq.rabbitmq.provider.ProviderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置[消费者]消息确认相关回调函数
 * 建议使用手动确认消费者消息
 */
@Configuration
public class MessageConsumerConfig {
    private Logger logger = LoggerFactory.getLogger(MessageConsumerConfig.class);

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Autowired
    private MyAckReceiver myAckReceiver; //消息处理类
    @Bean
    public SimpleMessageListenerContainer simpleMessageListenerContainer(){
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(1);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL); //RabbitMQ默认自动确认，此处改为手动确认消息
        //设置队列，此处的队列必须都是已经创建存在的
        container.setQueueNames(ProviderProperties.DIRECT_QUEUE_NAME);
        container.setMessageListener(myAckReceiver); //设置消息接收处理类
        return container;
    }
}
