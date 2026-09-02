package com.zongce.comprehensive.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 违规记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("penalty_record")
public class PenaltyRecord extends BaseEntity {

    /** 所属学生 id */
    private Long studentId;

    /** 违规名称 */
    private String name;

    /** 违规原因 */
    private String reason;

    /** 扣分分数（正数） */
    private BigDecimal score;

    /** 处分内容：无 / 通报批评 / 其他 */
    private String punishment;

    /** 描述信息 */
    private String description;
}
