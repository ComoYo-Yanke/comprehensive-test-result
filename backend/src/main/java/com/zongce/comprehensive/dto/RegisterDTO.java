package com.zongce.comprehensive.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 员工注册入参
 */
@Data
public class RegisterDTO {

    /** 工号 */
    @NotBlank(message = "工号不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码 id（由 /captcha 接口返回） */
    private String captchaId;

    /** 验证码内容 */
    @NotBlank(message = "请输入验证码")
    private String captchaCode;

    /** 真实姓名 */
    @NotBlank(message = "姓名不能为空")
    private String name;

    /** 电话号码 */
    private String phone;

    /** 身份证号 */
    private String number;

    /** 性别：1 男 2 女 */
    private Integer sex;

    /** 角色：1 教师 2 领导 3 辅导员 4 管理员 */
    private Integer role;

    /** 地址 */
    private String address;

    /** 邮箱 */
    private String email;

    /** 描述信息 */
    private String description;
}
