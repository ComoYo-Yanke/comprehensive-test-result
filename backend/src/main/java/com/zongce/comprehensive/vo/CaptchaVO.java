package com.zongce.comprehensive.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图形验证码返回：验证码 id + Base64 图片
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaVO {

    /** 验证码唯一标识（提交登录/注册时回传） */
    private String captchaId;

    /** 验证码图片（PNG，Base64，前端以 data:image/png;base64, 前缀展示） */
    private String imgBase64;
}
