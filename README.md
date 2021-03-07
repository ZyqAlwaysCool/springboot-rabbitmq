## SpringBoot-RabbitMQ
SpringBoot整合RabbitMQ，实现以下三类交换机及其队列，并设置手动消息确认模式。
RabbitMQ采用Docker启动。
### docker部署RabbitMQ
获取镜像
* `docker pull rabbitmq:management` #带management标签的镜像有管理页面

运行镜像
* `docker run -d -p 5672:5672 -p 15672:15672 --name rabbitmq rabbitmq:management`
    * 5672: rabbitmq监听端口
    * 15672: rabbitma管理页面监听端口，http://host:15672即可访问相应管理页面，默认用户名密码均为guest

参考
* [docker-rabbitmq](https://hub.docker.com/_/rabbitmq/)
* [docker安装部署RabbitMQ](https://www.jianshu.com/p/14ffe0f3db94)

### 三类交换机及队列
参考
* [SpringBoot整合RabbitMQ](https://blog.csdn.net/qq_35387940/article/details/100514134)
* [RabbitMQ及延迟队列](http://blog.battcn.com/2018/05/23/springboot/v2-queue-rabbitmq-delay/)

#### 相关依赖
>pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.4.3</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.zyq</groupId>
    <artifactId>springboot-rabbitmq</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>springboot-rabbitmq</name>
    <description>springboot整合rabbitmq</description>
    <properties>
        <java.version>11</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <!-- rabbitmq -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>

```
#### rabbitmq相关配置
>application.yml
```yaml
server:
  port: 18888

spring:
  rabbitmq:
    host: 172.20.46.192
    port: 5672
    username: guest # 默认为guest
    password: guest # 默认为guest
    #=====消息确认配置项(生产者推送消息成功、消费者接收消息成功)=====
    publisher-confirm-type: correlated #确认消息发送至交换机(exchange)
    publisher-returns: true # 确认消息发送至队列(queue)

```
#### 直连交换机(DirectExchange)
1.生产者(provider)
```java
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

```
2.消费者(consumer)
```java
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

```
#### 主题交换机(TopicExchange)
1.生产者(provider)
```java
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

```
2.消费者(consumer)
```java
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

```
#### 扇形交换机(FanoutExchange)
1.生产者(provider)
```java
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

```
2.消费者(consumer)
```java
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

```
#### 手动设置消息确认机制(生产者确认推送成功，消费者确认消费成功)
1.生产者(provider)
```java
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

```
2.消费者(consumer)
```java
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

```
#### 消息测试controller
>模拟消息推送
```java
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

```
#### 测试结果
```shell script
2021-03-06 22:24:08.774  INFO 29639 --- [io-18888-exec-1] c.z.r.controller.SendMessageController   : [sendDirectMessage][prepare for message]
2021-03-06 22:24:08.775  INFO 29639 --- [io-18888-exec-1] c.z.r.controller.SendMessageController   : [sendDirectMessage][message content] {msgCreateTime=2021-03-06 22-24-08, msgContent=this is a message-943190080, msgId=fd4a49d3-132d-4465-9f64-86d8d3ad0572}
2021-03-06 22:24:08.778  INFO 29639 --- [io-18888-exec-1] c.z.r.controller.SendMessageController   : [sendDirectMessage][convert and send message]
2021-03-06 22:24:08.795  INFO 29639 --- [io-18888-exec-1] c.z.r.controller.SendMessageController   : [sendDirectMessage][message send ok]
2021-03-06 22:24:08.806  INFO 29639 --- [nectionFactory1] c.z.r.c.provider.MessageProviderConfig   : [ConfirmCallback][相关数据: null 确认情况: true 原因: null]
2021-03-06 22:24:08.806  INFO 29639 --- [enerContainer-1] c.z.r.c.consumer.MyAckReceiver           : [MyAckReceiver.onMessage][message content][msgId: fd4a49d3-132d-4465-9f64-86d8d3ad0572 msgCreateTime: 2021-03-06 22-24-08 msgContent: this is a message-943190080]
2021-03-06 22:24:08.806  INFO 29639 --- [enerContainer-1] c.z.r.c.consumer.MyAckReceiver           : [消费的消息来源][directQueue]
```