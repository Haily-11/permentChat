package com.shanyangcode.realtimeservice.websocket;

import cn.hutool.json.JSONUtil;
import com.shanyangcode.common.constant.CommonConstant;
import com.shanyangcode.common.model.dto.MessageRequest;
import com.shanyangcode.realtimeservice.constants.WebSocketConstant;
import com.shanyangcode.realtimeservice.utils.SnowflakeDynamicUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

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

    /**
     * 处理心跳检测事件
     * @param ctx
     * @param evt
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        // 处理心跳
        if (evt instanceof IdleStateEvent event) {
            switch (event.state()) {
                case READER_IDLE:
                    log.error("读空闲超时");
                    clearChannel(ctx.channel());
                    ctx.close();
                    break;
                case WRITER_IDLE:
                    log.error("写空闲超时");
                case ALL_IDLE:
                    log.error("读写空闲超时");
            }
        }
    }

    /**
     * 发送消息到kafka
     * @param msg
     * @param channel
     */
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

    /**
     * 记录用户离线时间
     * @param userId
     */
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