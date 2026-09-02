package com.zongce.comprehensive;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 学生综测统计系统启动类
 *
 * @author zongce
 */
@SpringBootApplication
@MapperScan("com.zongce.comprehensive.mapper")   // 扫描 Mapper 接口
@EnableCaching                                    // 开启 Spring Cache 缓存
@EnableScheduling                                 // 开启定时任务
public class ComprehensiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComprehensiveApplication.class, args);
        System.out.println("========== 学生综测统计系统后端启动成功 ==========");
    }
}
