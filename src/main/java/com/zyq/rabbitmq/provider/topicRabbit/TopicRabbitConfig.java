package com.zyq.rabbitmq.provider.topicRabbit;

import com.zyq.rabbitmq.provider.ProviderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicRabbitConfig {
    private Logger logger = LoggerFactory.getLogger(TopicRabbitConfig.class);
    public static final String TOPIC_ROUTING_KEY = "topic.topicRoutingKey";

    @Bean
    public Queue topicQueueOne(){
        return new Queue(ProviderProperties.TOPIC_QUEUE_NAME+"One", true);
    }

    @Bean
    public Queue topicQueueTwo(){
        return new Queue(ProviderProperties.TOPIC_QUEUE_NAME+"Two", true);
    }

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(ProviderProperties.TOPIC_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Binding bindingTopicExchangeAndQueueOne(){
        return BindingBuilder.bind(topicQueueOne()).to(topicExchange()).with(TOPIC_ROUTING_KEY);
    }

    @Bean
    public Binding bindingTopicExchangeAndQueueTwo(){
        /**
         * 只要消息携带路由键以topic.开头都会分发至该队列中
         */
        return BindingBuilder.bind(topicQueueTwo()).to(topicExchange()).with("topic.#");
    }
}
