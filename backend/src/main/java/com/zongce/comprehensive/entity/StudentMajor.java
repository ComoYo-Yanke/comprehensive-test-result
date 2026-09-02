package com.zongce.comprehensive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 学生-专业 关联表（一个学生可修多个专业）
 */
@Data
@TableName("student_major")
public class StudentMajor implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学生 id */
    private Long studentId;

    /** 专业 id */
    private Long majorId;
}
