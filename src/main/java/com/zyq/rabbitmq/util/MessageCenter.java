package com.zyq.rabbitmq.util;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 消息中心，用于消息生成
 * @Author zyq
 * @Date 2021/3/6
 */
@Component
@Data
public class MessageCenter {
    public Map<String, String> getMessage(String msgContent){
        Map<String, String> msg = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        String msgId = UUID.randomUUID().toString();
        String msgCreateTime = sdf.format(new Date());
        msg.put("msgId", msgId);
        msg.put("msgCreateTime", msgCreateTime);
        msg.put("msgContent", msgContent);
        return msg;
    }
}
