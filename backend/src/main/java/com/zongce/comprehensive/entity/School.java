package com.zongce.comprehensive.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学院表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("school")
public class School extends BaseEntity {

    /** 学院名称 */
    private String name;

    /** 状态：1 正常 2 解散 3 未启用 */
    private Integer status;

    /** 描述信息 */
    private String description;
}
