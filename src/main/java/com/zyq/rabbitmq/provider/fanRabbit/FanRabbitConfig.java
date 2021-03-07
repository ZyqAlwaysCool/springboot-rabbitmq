package com.zyq.rabbitmq.provider.fanRabbit;

import com.zyq.rabbitmq.provider.ProviderProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 扇形交换机会将消息推送至所有绑定它上的队列中，无需设置路由键
 */
@Configuration
public class FanRabbitConfig {
    private Logger logger = LoggerFactory.getLogger(FanRabbitConfig.class);

    @Bean
    public Queue firstFanQueue(){
        return new Queue("first"+ProviderProperties.FAN_QUEUE_NAME, true);
    }

    @Bean
    public Queue sencondFanQueue(){
        return new Queue("second"+ProviderProperties.FAN_QUEUE_NAME, true);
    }

    @Bean
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange(ProviderProperties.FAN_EXCHANGE_NAME);
    }

    /**
     * 绑定第一个队列
     * @return
     */
    @Bean
    public Binding bindingFirstFanoutExchangeAndQueue(){
        return BindingBuilder.bind(firstFanQueue()).to(fanoutExchange());
    }

    /**
     * 绑定第二个队列
     * @return
     */
    @Bean
    public Binding bindingSecondFanoutExchangeAndQueue(){
        return BindingBuilder.bind(sencondFanQueue()).to(fanoutExchange());
    }
}
