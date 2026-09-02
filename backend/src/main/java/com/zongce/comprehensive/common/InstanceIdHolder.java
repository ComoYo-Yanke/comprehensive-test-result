package com.zongce.comprehensive.common;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 服务实例唯一标识
 * <p>用于 WebSocket 集群广播时区分消息来源，避免本机重复推送。</p>
 */
@Component
public class InstanceIdHolder {

    private final String instanceId = UUID.randomUUID().toString();

    public String getInstanceId() {
        return instanceId;
    }
}
