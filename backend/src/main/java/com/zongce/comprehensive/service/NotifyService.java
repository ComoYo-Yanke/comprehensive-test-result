package com.zongce.comprehensive.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zongce.comprehensive.common.InstanceIdHolder;
import com.zongce.comprehensive.config.RedisListenerConfig;
import com.zongce.comprehensive.entity.MessageNotification;
import com.zongce.comprehensive.mapper.MessageNotificationMapper;
import com.zongce.comprehensive.websocket.NotifyMessage;
import com.zongce.comprehensive.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 通知服务
 * <p>发送通知：①落库；②本地 WebSocket 推送；③Redis 发布广播给其它实例（集群广播）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyService {

    private final MessageNotificationMapper notificationMapper;
    private final WebSocketServer webSocketServer;
    private final StringRedisTemplate stringRedisTemplate;
    private final InstanceIdHolder instanceIdHolder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送通知
     *
     * @param receiverId   接收人 id
     * @param receiverType 接收人类型：1 学生 2 员工
     * @param title        标题
     * @param content      内容
     * @param type         通知类型（自定义数字）
     * @param relatedId    关联业务 id（可为 null）
     */
    public void send(Long receiverId, Integer receiverType, String title, String content,
                     Integer type, Long relatedId) {
        // 1. 落库
        MessageNotification notification = new MessageNotification();
        notification.setReceiverId(receiverId);
        notification.setReceiverType(receiverType);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(0);
        notification.setRelatedId(relatedId);
        notificationMapper.insert(notification);

        try {
            // 2. 构建消息并本地推送
            NotifyMessage message = new NotifyMessage(receiverId, receiverType, title, content,
                    type, relatedId, instanceIdHolder.getInstanceId());
            String json = objectMapper.writeValueAsString(message);
            String userType = receiverType == 1 ? "student" : "employee";
            webSocketServer.sendToUser(receiverId, userType, json);

            // 3. Redis 发布，其它实例订阅后推送（集群广播）
            stringRedisTemplate.convertAndSend(RedisListenerConfig.NOTIFY_CHANNEL, json);
        } catch (Exception e) {
            log.error("通知推送异常（已落库）", e);
        }
    }
}
