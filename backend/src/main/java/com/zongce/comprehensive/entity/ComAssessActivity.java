package com.zongce.comprehensive.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 综测活动记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_assess_activity")
public class ComAssessActivity extends BaseEntity {

    /** 活动名称 */
    private String name;

    /** 状态：1 未审核 2 审核通过 3 审核不通过 4 已取消 5 举办中 6 已结束 */
    private Integer status;

    /** 创办学院 id（校级活动为 null） */
    private Long schoolId;

    /** 类型：1 校级思想 2 校级文体 3 院级思想 4 院级文体 */
    private Integer type;

    /** 限制人数 */
    private Integer limitNum;

    /** 活动开始时间 */
    private LocalDateTime startTime;

    /** 活动结束时间 */
    private LocalDateTime endTime;

    /** 描述信息 */
    private String description;

    /** 创办学院名称（非表字段，用于展示） */
    @TableField(exist = false)
    private String schoolName;

    /** 已参加人数（非表字段，用于展示） */
    @TableField(exist = false)
    private Long joinedCount;

    /** 当前学生是否已参加（非表字段，用于展示） */
    @TableField(exist = false)
    private Boolean joined;

    /** 当前学生可得综测分（非表字段，用于展示） */
    @TableField(exist = false)
    private BigDecimal myScore;
}
