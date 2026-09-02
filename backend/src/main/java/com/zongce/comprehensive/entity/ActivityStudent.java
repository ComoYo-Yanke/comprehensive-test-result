package com.zongce.comprehensive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 活动-参加学生 关联表
 * <p>joinTime 记录加入时间（毫秒级），用于定时任务「踢出最后加入者」。</p>
 */
@Data
@TableName("activity_student")
public class ActivityStudent implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 活动 id */
    private Long activityId;

    /** 参加学生 id */
    private Long studentId;

    /** 加入时间（毫秒精度） */
    private LocalDateTime joinTime;
}
