package com.zyq.rabbitmq.configuration.consumer;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息接收处理类
 * 手动确认监听消息需要实现ChannelAwareMessageListener
 */
@Component
public class MyAckReceiver implements ChannelAwareMessageListener {
    private Logger logger = LoggerFactory.getLogger(MyAckReceiver.class);
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long deliverTag = message.getMessageProperties().getDeliveryTag();
        try{
            String msg = message.toString();
            String[] msgArray = msg.split("'");
            Map<String, String> msgMap = mapStringToMap(msgArray[1].trim(), 3);
            logger.info("[MyAckReceiver.onMessage][message content][msgId: {} msgCreateTime: {} msgContent: {}]",
                    msgMap.get("msgId"), msgMap.get("msgCreateTime"), msgMap.get("msgContent"));
            logger.info("[消费的消息来源][{}]", message.getMessageProperties().getConsumerQueue());
            channel.basicAck(deliverTag, true); //第二个参数，手动确认可以被批处理，当该参数为 true 时，则可以一次性确认 delivery_tag 小于等于传入值的所有消息
        }catch (Exception e){
            channel.basicReject(deliverTag, false);
            e.printStackTrace();
        }
    }

    //{key=value,key=value,key=value} 格式转换成map
    private Map<String, String> mapStringToMap(String str,int entryNum ) {
        str = str.substring(1, str.length() - 1);
        String[] strs = str.split(",", entryNum);
        Map<String, String> map = new HashMap<String, String>();
        for (String string : strs) {
            String key = string.split("=")[0].trim();
            String value = string.split("=")[1];
            map.put(key, value);
        }
        return map;
    }
}
