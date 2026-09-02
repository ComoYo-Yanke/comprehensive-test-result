package com.zongce.comprehensive.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * <p>跨域、拦截器注册、静态资源映射。</p>
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    /** 上传目录 */
    @Value("${zongce.upload-dir:uploads}")
    private String uploadDir;

    /** 跨域配置：前后端分离，允许前端两个端访问 */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /** 注册拦截器：JWT 鉴权 + 接口限流；放行公开接口与文档 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // JWT 拦截器先执行：写登录上下文并做权限校验
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/auth/login",          // 登录
                        "/api/v1/auth/student/login",
                        "/api/v1/auth/employee/login",
                        "/api/v1/auth/employee/register",
                        "/api/v1/captcha",             // 图形验证码
                        "/api/v1/captcha/**",
                        "/uploads/**"                   // 上传的佐证图片
                );
        // 限流拦截器随后执行：登录/注册等公开接口按 IP 限流，业务写接口按登录用户限流
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**");
    }

    /** 上传文件静态资源映射 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
