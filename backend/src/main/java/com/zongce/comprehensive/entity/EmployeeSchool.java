package com.zongce.comprehensive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 员工-学院 关联表（一个员工可任职多个学院）
 */
@Data
@TableName("employee_school")
public class EmployeeSchool implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 员工 id */
    private Long employeeId;

    /** 学院 id */
    private Long schoolId;
}
