package com.whim.mybatisplus.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.permission.DataPermissionProvider;
import com.whim.mybatisplus.handler.AutoFillFieldHandler;
import com.whim.mybatisplus.permission.DataPermissionAspect;
import com.whim.mybatisplus.permission.DataPermissionInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 通用数据权限、分页、乐观锁与审计配置。
 */
@AutoConfiguration
public class MybatisPlusConfiguration {
    /** 数据权限先于分页执行，保证分页总数与结果范围一致。 */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new DataPermissionInterceptor());
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

    /** 延迟获取业务权限解析器，保持认证模块与持久化模块独立。 */
    @Bean
    public DataPermissionAspect dataPermissionAspect(ObjectProvider<DataPermissionProvider> providers) {
        return new DataPermissionAspect(providers);
    }

    /** 从统一认证抽象填充操作者与时间，不将审计字段作为业务归属。 */
    @Bean
    public MetaObjectHandler autoFillFieldHandler(AuthenticationContext authenticationContext) {
        return new AutoFillFieldHandler(authenticationContext);
    }
}
