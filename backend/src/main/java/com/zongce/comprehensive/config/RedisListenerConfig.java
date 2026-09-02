package com.zongce.comprehensive.config;

import com.zongce.comprehensive.websocket.RedisNotifyListener;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * Redis 消息监听配置（WebSocket 集群广播用）
 */
@Configuration
@RequiredArgsConstructor
public class RedisListenerConfig {

    /** 广播频道 */
    public static final String NOTIFY_CHANNEL = "zongce:notify";

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory factory, RedisNotifyListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(listener, new ChannelTopic(NOTIFY_CHANNEL));
        return container;
    }
}
