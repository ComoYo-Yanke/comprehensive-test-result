package com.zongce.comprehensive.controller;

import com.zongce.comprehensive.common.RateLimit;
import com.zongce.comprehensive.common.Result;
import com.zongce.comprehensive.service.CaptchaService;
import com.zongce.comprehensive.vo.CaptchaVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 图形验证码接口（公开，登录/注册前获取）
 */
@Tag(name = "验证码接口")
@RestController
@RequestMapping("/api/v1/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    @Operation(summary = "获取图形验证码")
    @RateLimit(scope = RateLimit.Scope.IP, key = "captcha", limit = 30, windowSeconds = 60,
            message = "验证码获取过于频繁，请稍后再试")
    @GetMapping
    public Result<CaptchaVO> captcha() {
        return Result.success(captchaService.generate());
    }
}
