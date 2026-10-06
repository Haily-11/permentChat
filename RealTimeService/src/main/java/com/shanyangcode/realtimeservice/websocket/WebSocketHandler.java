package com.shanyangcode.realtimeservice.websocket;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.json.JSONUtil;
import com.shanyangcode.common.constant.CommonConstant;
import com.shanyangcode.common.model.dto.MessageRequest;
import com.shanyangcode.realtimeservice.constants.WebSocketConstant;
import com.shanyangcode.realtimeservice.utils.SnowflakeDynamicUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;

@AllArgsConstructor
@Slf4j
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        Channel channel = channelHandlerContext.channel();
        String msg = textWebSocketFrame.text();

        try{
            //心跳检测
            if(WebSocketConstant.HEARTBEAT_PING.equals(msg)){
                if(channel.isActive()){
                    log.debug("Received heartbeat ping from {}",channel.id());
                    //检测道心跳后就发送回复pong
                    channel.writeAndFlush(new TextWebSocketFrame(WebSocketConstant.HEARTBEAT_PONG));
                }
            }else{
                //发送消息到kafka
                if(channel.isActive()){
                    sendMessageKafKa(msg,channel);
                }else{
                    log.warn("Channel {} inactive, skip message: {}", channel.id(), msg);
                }

            }
        }catch(Exception e){
            log.error("Error handling message from {}: {}", channel.id(), msg, e);
            clearChannel(channel);
        }
    }

    private void sendMessageKafKa(String msg, Channel channel) {

        //先转为json
        MessageRequest msgRequest = JSONUtil.toBean(msg, MessageRequest.class);
        msgRequest.setMessageId(SnowflakeDynamicUtil.nextId());
        msgRequest.setCreatedTime(new Date());

        //消息存储
        kafkaTemplate.send(CommonConstant.KAFKA_MESSAGE_TOPIC_STORE,JSONUtil.toJsonStr(msgRequest)).whenComplete((success,failure)->{
            if (failure != null) {
                // 生产者生产失败
                System.err.println("生产者生产失败: " + failure.getMessage());
                // 记录日志、告警、补偿等
            } else {
                // 生产者生产成功
                System.out.println("生产者生产成功，offset: " + success.getRecordMetadata().offset());
            }
        });

        //消息推送
        kafkaTemplate.send(CommonConstant.KAFKA_MESSAGE_TOPIC_PUSH,msgRequest.getSessionId().toString(),JSONUtil.toJsonStr(msgRequest)).whenComplete((success,failure)->{
            if (failure != null) {
                // 生产者生产失败
                System.err.println("生产者生产失败: " + failure.getMessage());
                // 记录日志、告警、补偿等
            } else {
                // 生产者生产成功
                System.out.println("生产者生产成功消息推送，offset: " + success.getRecordMetadata().offset());
            }
        });


    }

    private void clearChannel(Channel channel) {
        if(channel==null){
            return;
        }
        System.out.println("clearChannel:"+channel.id());

        String userId = ChannelManager.getUserIdByChannel(channel);
        try{
            //双向清除channel和userId
            if(userId !=null){
                ChannelManager.removeChannelByUser(userId);
                saveOfflineTime(userId);
            }
            ChannelManager.removeUserByChannel(channel);
        }catch (Exception e){
            log.error("clearChannel failed for channel: {},userId: {}",channel,userId);
        }finally {
            //关闭channel
            if(channel.isActive()){
                channel.close();
            }
        }
    }

    private void saveOfflineTime(String userId) {
        String key = CommonConstant.OFFLINE_KEY_REDIS + userId;
        String timestamp = String.valueOf(System.currentTimeMillis());//System.currentTimeMillis()返回的是long
        stringRedisTemplate.opsForValue().set(key,timestamp);
        log.debug("记录用户离线时间: userId={}, timestamp={}", userId, timestamp);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception{
        super.handlerAdded(ctx);
        System.out.println("handler added");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception{
        super.channelActive(ctx);
        System.out.println("channel active");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception{
        super.handlerRemoved(ctx);
        System.out.println("handler removed");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{
        super.channelActive(ctx);
        System.out.println("channel inActive");
    }
}