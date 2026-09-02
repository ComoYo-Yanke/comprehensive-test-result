package com.zongce.comprehensive.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类
 * <p>统一主键与审计字段（创建/更新时间、创建/更新人），由 MetaObjectHandler 自动填充。</p>
 */
@Data
public class BaseEntity implements Serializable {

    /** 主键 id */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT)
    private Long createUser;

    /** 更新人 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUser;
}
