package com.zongce.comprehensive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 活动-负责学生 关联表（一个活动可由多个学生负责）
 */
@Data
@TableName("activity_charge_student")
public class ActivityChargeStudent implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 活动 id */
    private Long activityId;

    /** 负责学生 id */
    private Long studentId;
}
