package com.zongce.comprehensive.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 员工表（管理员/教师/辅导员/领导）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("employee")
public class Employee extends BaseEntity {

    /** 工号（数字，可用作登录账号） */
    private String username;

    /** 密码（BCrypt 加密，仅写入不出参） */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** 真实姓名 */
    private String name;

    /** 电话号码 */
    private String phone;

    /** 身份证号 */
    private String number;

    /** 性别：1 男 2 女 */
    private Integer sex;

    /** 状态：1 在职 2 离职 3 停用 */
    private Integer status;

    /** 角色：1 教师 2 领导 3 辅导员 4 管理员 */
    private Integer role;

    /** 辅导员专属管理的班级 id */
    private Long clazzId;

    /** 地址 */
    private String address;

    /** 邮箱 */
    private String email;

    /** 描述信息 */
    private String description;

    /** 任职学院名称，逗号分隔（非表字段，用于展示） */
    @TableField(exist = false)
    private String schoolNames;

    /** 管理班级名称（非表字段，用于展示） */
    @TableField(exist = false)
    private String clazzName;

    /** 任职学院 id 列表（非表字段，用于编辑回显/勾选） */
    @TableField(exist = false)
    private List<Long> schoolIds;
}
