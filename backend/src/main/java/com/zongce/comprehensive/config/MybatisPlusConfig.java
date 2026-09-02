package com.zongce.comprehensive.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zongce.comprehensive.common.UserContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置
 * <p>
 * 1. 分页插件；
 * 2. 公共字段（创建/更新时间、创建/更新人）自动填充。
 * </p>
 */
@Configuration
public class MybatisPlusConfig {

    /** 分页插件 */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        pagination.setMaxLimit(500L);   // 单页最大 500 条
        interceptor.addInnerInterceptor(pagination);
        return interceptor;
    }

    /** 公共字段自动填充 */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                LocalDateTime now = LocalDateTime.now();
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
                Long userId = UserContext.getUserId();
                this.strictInsertFill(metaObject, "createUser", Long.class, userId == null ? 0L : userId);
                this.strictInsertFill(metaObject, "updateUser", Long.class, userId == null ? 0L : userId);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                Long userId = UserContext.getUserId();
                this.strictUpdateFill(metaObject, "updateUser", Long.class, userId == null ? 0L : userId);
            }
        };
    }
}
