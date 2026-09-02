package com.zongce.comprehensive.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 服务端
 * <p>维护「用户 -> 会话」的映射，支持按用户推送通知。</p>
 */
@Slf4j
@Component
public class WebSocketServer extends TextWebSocketHandler {

    /** 会话缓存：key = 用户类型:用户id */
    private static final ConcurrentHashMap<String, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    /** 连接建立：从握手属性中取用户信息并登记 */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String key = buildKey(session);
        if (key != null) {
            SESSIONS.put(key, session);
            log.info("WebSocket 连接建立: {}", key);
        }
    }

    /** 收到客户端消息：仅做简单回显/忽略 */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 客户端心跳等消息，此处忽略处理
    }

    /** 连接关闭：移除会话 */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String key = buildKey(session);
        if (key != null) {
            SESSIONS.remove(key);
            log.info("WebSocket 连接关闭: {}", key);
        }
    }

    /** 传输异常：移除会话 */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String key = buildKey(session);
        if (key != null) {
            SESSIONS.remove(key);
        }
    }

    /**
     * 向指定用户推送消息（按类型+id定位会话）
     */
    public boolean sendToUser(Long userId, String userType, String message) {
        WebSocketSession session = SESSIONS.get(userType + ":" + userId);
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(message));
                }
                return true;
            } catch (Exception e) {
                log.error("WebSocket 推送失败", e);
                return false;
            }
        }
        return false;
    }

    /** 从会话中提取用户标识 key */
    private String buildKey(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        Object userType = session.getAttributes().get("userType");
        if (userId == null || userType == null) {
            return null;
        }
        return userType + ":" + userId;
    }
}
