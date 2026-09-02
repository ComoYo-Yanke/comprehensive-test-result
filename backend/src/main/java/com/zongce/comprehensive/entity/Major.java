package com.zongce.comprehensive.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 专业表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("major")
public class Major extends BaseEntity {

    /** 专业名称 */
    private String name;

    /** 所属学院 id */
    private Long schoolId;

    /** 描述信息 */
    private String description;

    /** 所属学院名称（非表字段，用于展示） */
    @TableField(exist = false)
    private String schoolName;
}
