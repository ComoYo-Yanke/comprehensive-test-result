package com.zongce.comprehensive.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 综测成绩表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_assess_score")
public class ComAssessScore extends BaseEntity {

    /** 所属学生 id */
    private Long studentId;

    /** 年份 */
    private Integer year;

    /** 学期：1 春季(上半年) 2 秋季(下半年) */
    private Integer semester;

    /** 活动分（校级 0.2 / 院级 0.1 累加） */
    private BigDecimal activityScore;

    /** 其他加分 */
    private BigDecimal extraScore;

    /** 违规扣分 */
    private BigDecimal penaltyScore;

    /** 总成绩 = 活动分 + 其他加分 - 违规扣分 */
    private BigDecimal score;

    /** 审核状态：1 未审核 2 审核通过 3 审核不通过 */
    private Integer status;

    /** 审核不通过原因 */
    private String reason;

    /** 审核员工 id */
    private Long reviewerId;

    /** 描述信息 */
    private String description;

    /** 学生学号（非表字段，审核列表展示用） */
    @TableField(exist = false)
    private String studentUsername;

    /** 学生姓名（非表字段，审核列表展示用） */
    @TableField(exist = false)
    private String studentName;
}
