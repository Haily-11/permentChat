package com.shanyangcode.offlinedataservice.comsumer;

import cn.hutool.json.JSONUtil;
import com.shanyangcode.common.model.dto.MessageRequest;
import com.shanyangcode.offlinedataservice.service.MessageService;
import jakarta.annotation.Resource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ConsumerOfflineService {

    @Resource
    private MessageService messageService;

    @KafkaListener(topics = "store-topic",groupId = "infinite-chat-store-group")
    public void consume(String msg){
        System.out.println("Consumed message store:"+msg);
        MessageRequest msgRequest = JSONUtil.toBean(msg, MessageRequest.class);
        messageService.savaMessageToMySQL(msgRequest);
        System.out.println(msgRequest);
    }
}
