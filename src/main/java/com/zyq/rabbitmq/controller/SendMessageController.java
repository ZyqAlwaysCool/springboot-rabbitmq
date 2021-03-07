package com.zyq.rabbitmq.controller;

import com.zyq.rabbitmq.provider.ProviderProperties;
import com.zyq.rabbitmq.provider.directRabbit.DirectRabbitConfig;
import com.zyq.rabbitmq.provider.topicRabbit.TopicRabbitConfig;
import com.zyq.rabbitmq.util.MessageCenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;

/**
 * 此Controller用于发送消息至RabbitMQ消息队列
 * @Author zyq
 * @Date 2021/3/6
 */
@RestController
@RequestMapping("/message-controller")
public class SendMessageController {
    @Autowired
    private RabbitTemplate rabbitTemplate; //使用RabbitTemplate提供接收/发送等方法
    private Logger logger = LoggerFactory.getLogger(SendMessageController.class);
    @Autowired
    private MessageCenter messageCenter;

    @GetMapping("/sendDirectMessage")
    public String sendDirectMessage(){
        logger.info("[sendDirectMessage][prepare for message]");
        Map<String, String> msg = messageCenter.getMessage("this is a message-" + new Random().nextInt());
        logger.info("[sendDirectMessage][message content] {}", msg);
        logger.info("[sendDirectMessage][convert and send message]");
        rabbitTemplate.convertAndSend(ProviderProperties.DIRECT_EXCHANGE_NAME,
                DirectRabbitConfig.DIRECT_ROUTING_KEY,
                msg);
        logger.info("[sendDirectMessage][message send ok]");
        return "direct message send";
    }

    @GetMapping("/sendTopicMessage")
    public String sendTopicMessage(){
        /**
         * 消息会推送至两个队列中
         */
        logger.info("[sendTopicMessage][prepare for message]");
        Map<String, String> msg = messageCenter.getMessage("this is a message-" + new Random().nextInt(100));
        logger.info("[sendTopicMessage][message content] {}", msg);
        logger.info("[sendTopicMessage][convert and send message]");
        rabbitTemplate.convertAndSend(ProviderProperties.TOPIC_EXCHANGE_NAME,
                TopicRabbitConfig.TOPIC_ROUTING_KEY,
                msg);
        logger.info("[sendTopicMessage][message send ok]");
        return "topic message send";
    }

    @GetMapping("/sendFanoutMessage")
    public String sendFanoutMessage(){
        logger.info("[sendFanoutMessage][prepare for message]");
        Map<String, String> msg = messageCenter.getMessage("this is a message-" + new Random().nextInt());
        logger.info("[sendFanoutMessage][message content] {}", msg);
        logger.info("[sendFanoutMessage][convert and send message]");
        //扇形交换机无需设置路由键，会发布到所有绑定它的消息队列中
        rabbitTemplate.convertAndSend(ProviderProperties.FAN_EXCHANGE_NAME, null, msg);
        logger.info("[sendFanoutMessage][message send ok]");
        return "Fanout message send";
    }
}
