package com.shanyangcode.userservice.consumer;

import cn.hutool.json.JSONUtil;
import com.shanyangcode.userservice.model.dto.FriendRequestCreationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;

import static com.shanyangcode.userservice.constant.KafkaTopicConstant.GROUP_FRIEND_REQUEST_ENQUEUER;
import static com.shanyangcode.userservice.constant.KafkaTopicConstant.TOPIC_FRIEND_REQUEST_CREATION;

@RequiredArgsConstructor
@Slf4j
public class FriendRequestExpirationEnqueuer {

    private final StringRedisTemplate redisTemplate;
    private static final String ZSET_KEY = "friend-request-expire-zset";
    /**
     * 消费好友申请创建事件，注册延迟过期任务
     * <p>
     * 处理流程：
     * 1. 解析Kafka消息，获取好友申请ID和过期时间
     * 2. 使用ZADD将好友申请ID添加到ZSET
     * 3. score设置为过期时间戳（毫秒）
     * 4. 定时任务会定期扫描ZSET，取出已到期的ID
     *
     * @param message Kafka消息（JSON格式的FriendRequestCreationEvent）
     */
    @KafkaListener(
            topics = TOPIC_FRIEND_REQUEST_CREATION,
            groupId = GROUP_FRIEND_REQUEST_ENQUEUER,
            concurrency = "1"  // 单线程消费，保证顺序
    )
    public void enqueueExpirationTask(String message) {
        try {
            log.info("收到好友申请创建事件: {}", message);

            // 1. 解析事件
            FriendRequestCreationEvent event = JSONUtil.toBean(message, FriendRequestCreationEvent.class);
            Long applyFriendId = event.getApplyFriendId();
            Long expireTime = event.getExpireTime();

            if (applyFriendId == null || expireTime == null) {
                log.error("好友申请事件数据不完整，applyFriendId: {}, expireTime: {}", applyFriendId, expireTime);
                return;
            }

            if (expireTime <= System.currentTimeMillis()) {
                log.warn("好友申请已过期，跳过注册，申请ID: {}, 过期时间: {}", applyFriendId, expireTime);
                return;
            }

            // 2. 注册到 Redis ZSET
            // ZADD friend-request-expire-zset <expireTime> <applyFriendId>
            Boolean result = redisTemplate.opsForZSet().add(
                    ZSET_KEY,
                    String.valueOf(applyFriendId),
                    expireTime.doubleValue()
            );

            if (Boolean.TRUE.equals(result)) {
                log.info("好友申请过期任务注册成功，申请ID: {}, 过期时间: {}", applyFriendId, expireTime);
            } else {
                log.info("好友申请过期任务注册失败，申请ID: {}, 过期时间: {}", applyFriendId, expireTime);
            }

        } catch (Exception e) {
            log.error("处理好友申请创建事件失败: {}, 错误: {}", message, e.getMessage(), e);
        }
}
}
