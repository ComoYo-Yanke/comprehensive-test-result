package com.zongce.comprehensive.vo;

import lombok.Data;

/**
 * 登录返回结果
 */
@Data
public class LoginVO {

    /** JWT token */
    private String token;

    /** 用户 id */
    private Long userId;

    /** 用户类型：student / employee */
    private String userType;

    /** 角色（数字） */
    private Integer role;

    /** 登录账号（学号 / 工号） */
    private String username;

    /** 姓名 */
    private String name;
}
