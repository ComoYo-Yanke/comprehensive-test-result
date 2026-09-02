package com.zongce.comprehensive.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * WebSocket 推送消息体（用于集群广播，经 Redis 序列化传递）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifyMessage implements Serializable {

    /** 接收人 id */
    private Long receiverId;

    /** 接收人类型：1 学生 2 员工 */
    private Integer receiverType;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 通知类型 */
    private Integer type;

    /** 关联业务 id */
    private Long relatedId;

    /** 消息来源实例 id（用于本机去重） */
    private String sourceInstanceId;
}
