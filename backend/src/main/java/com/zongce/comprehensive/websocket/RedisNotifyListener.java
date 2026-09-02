package com.zongce.comprehensive.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zongce.comprehensive.common.InstanceIdHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

/**
 * Redis 发布订阅监听器
 * <p>用于 WebSocket 集群广播：本机产生的消息会直接本地推送，其他实例的消息
 * 通过 Redis 订阅收到后，在本机找到对应会话并推送。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisNotifyListener implements MessageListener {

    private final WebSocketServer webSocketServer;
    private final InstanceIdHolder instanceIdHolder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            NotifyMessage notify = objectMapper.readValue(message.getBody(), NotifyMessage.class);
            // 本机自己发的消息已直接推送，跳过避免重复
            if (instanceIdHolder.getInstanceId().equals(notify.getSourceInstanceId())) {
                return;
            }
            String userType = notify.getReceiverType() == 1 ? "student" : "employee";
            webSocketServer.sendToUser(notify.getReceiverId(), userType, objectMapper.writeValueAsString(notify));
        } catch (Exception e) {
            log.error("处理 Redis 广播消息失败", e);
        }
    }
}
