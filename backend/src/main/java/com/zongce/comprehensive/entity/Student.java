package com.zongce.comprehensive.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 学生表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("student")
public class Student extends BaseEntity {

    /** 学号（数字，可用作登录账号） */
    private String username;

    /** 密码（BCrypt 加密，仅写入不出参） */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** 姓名 */
    private String name;

    /** 电话号码 */
    private String phone;

    /** 身份证号 */
    private String number;

    /** 性别：1 男 2 女 */
    private Integer sex;

    /** 状态：1 在读 2 毕业 3 开除 4 休学 */
    private Integer status;

    /** 角色：1 普通学生 2 学生会成员 3 社团成员 4 外国留学生 */
    private Integer role;

    /** 班级 id */
    private Long clazzId;

    /** 学院 id */
    private Long schoolId;

    /** 入学时间（年月，如 2023-09） */
    private String enrollTime;

    /** 地址 */
    private String address;

    /** 邮箱 */
    private String email;

    /** 描述信息 */
    private String description;

    /** 班级名称（非表字段，用于展示） */
    @TableField(exist = false)
    private String clazzName;

    /** 学院名称（非表字段，用于展示） */
    @TableField(exist = false)
    private String schoolName;

    /** 专业名称，逗号分隔（非表字段，用于展示） */
    @TableField(exist = false)
    private String majorNames;

    /** 专业 id 列表（非表字段，用于编辑回显/勾选） */
    @TableField(exist = false)
    private List<Long> majorIds;
}
