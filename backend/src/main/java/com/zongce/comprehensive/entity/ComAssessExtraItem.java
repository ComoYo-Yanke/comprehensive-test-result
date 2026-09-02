package com.zongce.comprehensive.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 其他综测加分事项表（志愿活动、期末成绩等）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("com_assess_extra_item")
public class ComAssessExtraItem extends BaseEntity {

    /** 加分项名称 */
    private String name;

    /** 所属学生 id */
    private Long studentId;

    /** 佐证材料（图片 URL） */
    private String evidence;

    /** 加分分数 */
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
