package com.whim.mybatis.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.whim.core.factory.YmlPropertySourceFactory;
import com.whim.mybatis.aspect.DataPermissionAspect;
import com.whim.mybatis.handler.AutoFillFieldHandler;
import com.whim.mybatis.handler.CustomDataPermissionHandler;
import com.whim.mybatis.interceptor.CustomDataPermissionInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 * @author jince
 * @date 2025/6/17 17:12
 * @description mybatis plus 配置
 */
@AutoConfiguration
@PropertySource(value = "classpath:mybatis-plus.yml", factory = YmlPropertySourceFactory.class)
public class MybatisPlusConfiguration {
    @Bean
    public MybatisPlusInterceptor getMybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        // 分页插件
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        // 乐观锁插件
        mybatisPlusInterceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 数据权限插件
        mybatisPlusInterceptor.addInnerInterceptor(new CustomDataPermissionInterceptor(new CustomDataPermissionHandler()));
        // 防全表更新与删除插件
        mybatisPlusInterceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return mybatisPlusInterceptor;
    }

    /**
     * 自动填充处理器
     */
    @Bean
    public MetaObjectHandler AutoFillFieldHandler() {
        return new AutoFillFieldHandler();
    }

    /**
     * 数据权限切面
     */
    @Bean
    public DataPermissionAspect dataPermissionAspect() {
        return new DataPermissionAspect();
    }

}
