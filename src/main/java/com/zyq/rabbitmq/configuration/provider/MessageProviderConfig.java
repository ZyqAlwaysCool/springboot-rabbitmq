package com.zyq.rabbitmq.configuration.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置[生产者]消息确认相关回调函数
 */
@Configuration
public class MessageProviderConfig {
    private Logger logger = LoggerFactory.getLogger(MessageProviderConfig.class);

    @Bean
    public RabbitTemplate myRabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionFactory);
        //设置开启Mandatory才能触发回调函数，无论消息推送结果如何都强制调用回调函数
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback((correlationData, b, s) -> {
            logger.info("[ConfirmCallback][相关数据: {} 确认情况: {} 原因: {}]", correlationData, b, s);
        });
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returnedMessage) {
                logger.info("[ReturnCallback][消息: {} 回应码: {} 回应消息: {} 交换机: {} 路由键: {}]",
                        returnedMessage.getMessage(),
                        returnedMessage.getReplyCode(),
                        returnedMessage.getReplyText(),
                        returnedMessage.getExchange(),
                        returnedMessage.getRoutingKey());
            }
        });
        return rabbitTemplate;
    }
}
