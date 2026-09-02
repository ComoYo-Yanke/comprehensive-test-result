package com.zongce.comprehensive.controller;

import com.zongce.comprehensive.common.RateLimit;
import com.zongce.comprehensive.common.Result;
import com.zongce.comprehensive.dto.LoginDTO;
import com.zongce.comprehensive.dto.RegisterDTO;
import com.zongce.comprehensive.service.AuthService;
import com.zongce.comprehensive.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 */
@Tag(name = "认证接口")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "学生登录")
    @RateLimit(scope = RateLimit.Scope.IP, key = "login-student", limit = 10, windowSeconds = 60,
            message = "登录尝试过于频繁，请稍后再试")
    @PostMapping("/student/login")
    public Result<LoginVO> studentLogin(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.studentLogin(dto));
    }

    @Operation(summary = "员工登录")
    @RateLimit(scope = RateLimit.Scope.IP, key = "login-employee", limit = 10, windowSeconds = 60,
            message = "登录尝试过于频繁，请稍后再试")
    @PostMapping("/employee/login")
    public Result<LoginVO> employeeLogin(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.employeeLogin(dto));
    }

    @Operation(summary = "员工注册")
    @RateLimit(scope = RateLimit.Scope.IP, key = "register", limit = 5, windowSeconds = 60,
            message = "注册过于频繁，请稍后再试")
    @PostMapping("/employee/register")
    public Result<Void> employeeRegister(@Valid @RequestBody RegisterDTO dto) {
        authService.employeeRegister(dto);
        return Result.success();
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/info")
    public Result<Object> info() {
        return Result.success(authService.currentUser());
    }
}
