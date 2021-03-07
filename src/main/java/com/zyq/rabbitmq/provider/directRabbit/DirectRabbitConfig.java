package com.zyq.rabbitmq.provider.directRabbit;

import com.zyq.rabbitmq.provider.ProviderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectRabbitConfig {
    private Logger logger  = LoggerFactory.getLogger(DirectRabbitConfig.class);
    public static final String DIRECT_ROUTING_KEY = "directRoutingKey";
    @Bean
    public Queue directQueue(){
        /**
         * durable: 是否持久化，默认为false
         * exclusive: 只能被当前创建的连接使用，当连接关闭后队列删除，默认为false
         * autoDelete: 是否自动删除，当没有生产者或消费者使用此队列时该队列会自动删除
         * 一般情况下只需要设置队列持久化，其他默认为false
         */
        return new Queue(ProviderProperties.DIRECT_QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange(ProviderProperties.DIRECT_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Binding bindingDirectExchangeAndQueue(){
        /**
         * 队列和交换机进行绑定并设置用于匹配的路由键(DIRECT_ROUTING_KEY)
         */
        return BindingBuilder.bind(directQueue()).to(directExchange()).with(DIRECT_ROUTING_KEY);
    }
}
