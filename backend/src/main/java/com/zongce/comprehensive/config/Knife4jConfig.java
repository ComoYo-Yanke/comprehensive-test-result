package com.zongce.comprehensive.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 接口文档配置
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("学生综测统计系统接口文档")
                .description("高校学生综合素质测评系统 RESTful API")
                .version("1.0.0")
                .contact(new Contact().name("zongce")));
    }
}
