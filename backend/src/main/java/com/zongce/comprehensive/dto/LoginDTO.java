package com.zongce.comprehensive.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录入参
 */
@Data
public class LoginDTO {

    /** 账号（学号 / 工号） */
    @NotBlank(message = "账号不能为空")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    private String password;

    /** 验证码 id（由 /captcha 接口返回） */
    private String captchaId;

    /** 验证码内容 */
    @NotBlank(message = "请输入验证码")
    private String captchaCode;
}
