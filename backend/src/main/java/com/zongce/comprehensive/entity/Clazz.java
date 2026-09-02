package com.zongce.comprehensive.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班级表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("clazz")
public class Clazz extends BaseEntity {

    /** 班级名称 */
    private String name;

    /** 状态：1 在读 2 撤班 3 已毕业 */
    private Integer status;

    /** 所属学院 id */
    private Long schoolId;

    /** 所属专业 id */
    private Long majorId;

    /** 描述信息 */
    private String description;

    /** 所属学院名称（非表字段，用于展示） */
    @TableField(exist = false)
    private String schoolName;

    /** 所属专业名称（非表字段，用于展示） */
    @TableField(exist = false)
    private String majorName;
}
