package com.zyq.rabbitmq.provider;

import lombok.Data;
import org.springframework.stereotype.Component;

public class ProviderProperties {
    private ProviderProperties() {}
    public static final String DIRECT_QUEUE_NAME = "directQueue";
    public static final String DIRECT_EXCHANGE_NAME = "directExchange";
    public static final String TOPIC_QUEUE_NAME = "topicQueue";
    public static final String TOPIC_EXCHANGE_NAME = "topicExchange";
    public static final String FAN_QUEUE_NAME = "fanQueue";
    public static final String FAN_EXCHANGE_NAME = "fanExchange";
}
