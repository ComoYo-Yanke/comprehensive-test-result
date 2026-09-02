package com.zongce.comprehensive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 活动-负责老师 关联表（一个活动可由多个员工负责）
 */
@Data
@TableName("activity_employee")
public class ActivityEmployee implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 活动 id */
    private Long activityId;

    /** 负责老师（员工）id */
    private Long employeeId;
}
