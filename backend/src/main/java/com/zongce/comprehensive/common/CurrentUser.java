package com.zongce.comprehensive.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 当前登录用户信息（存入 JWT，解析后放入 ThreadLocal）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser implements Serializable {

    /** 用户主键 id */
    private Long userId;
    /** 用户类型：student / employee */
    private String userType;
    /** 角色（数字，含义见枚举） */
    private Integer role;
    /** 登录账号（学号 / 工号） */
    private String username;
}
