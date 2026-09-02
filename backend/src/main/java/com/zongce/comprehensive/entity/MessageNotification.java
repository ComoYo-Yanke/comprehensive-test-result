package com.zongce.comprehensive.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息通知表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_notification")
public class MessageNotification extends BaseEntity {

    /** 接收人 id */
    private Long receiverId;

    /** 接收人类型：1 学生 2 员工 */
    private Integer receiverType;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String content;

    /** 通知类型（自定义数字，用于前端区分展示） */
    private Integer type;

    /** 是否已读：0 未读 1 已读 */
    private Integer isRead;

    /** 关联业务 id */
    private Long relatedId;
}
