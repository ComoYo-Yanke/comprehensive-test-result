package com.zongce.comprehensive.dto;

import lombok.Data;

/**
 * 学生自助修改个人信息入参
 * <p>仅允许修改电话、邮箱、密码、描述。</p>
 */
@Data
public class SelfUpdateDTO {

    /** 电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 描述信息 */
    private String description;

    /** 原密码（修改密码时需校验） */
    private String oldPassword;

    /** 新密码 */
    private String newPassword;
}
