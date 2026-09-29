package com.whim.mybatisplus.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.exception.TenantAccessDeniedException;
import com.whim.mybatisplus.handler.AutoFillFieldHandler;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.update.Update;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/29
 * @description MyBatis-Plus 拦截器与审计字段自动填充配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(TenantIsolationProperties.class)
public class MybatisPlusConfiguration {

    /** 为明确配置的独立租户业务表追加默认租户边界。 */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(AuthenticationContext authenticationContext,
                                                         TenantIsolationProperties tenantProperties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        Set<String> protectedTables = tenantProperties.getProtectedTables().stream()
                .map(table -> table.toLowerCase(Locale.ROOT)).collect(java.util.stream.Collectors.toUnmodifiableSet());
        if (!protectedTables.isEmpty()) {
            interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
                /** 当前请求必须先选定租户，后台操作不能隐式访问租户表。 */
                @Override
                public Expression getTenantId() {
                    if (!authenticationContext.isLogin()) {
                        throw new TenantAccessDeniedException("缺少当前租户认证上下文");
                    }
                    UserInfo currentUser = authenticationContext.getCurrentUserInfo();
                    if (currentUser.getCurrentTenantId() == null) {
                        throw new TenantAccessDeniedException("当前会话尚未选择租户");
                    }
                    return new LongValue(currentUser.getCurrentTenantId());
                }

                /** 只拦截已审核 SQL 使用方式的独立租户业务表。 */
                @Override
                public boolean ignoreTable(String tableName) {
                    return !protectedTables.contains(tableName.toLowerCase(Locale.ROOT));
                }

                /** 受保护表由拦截器写入租户列，显式提供租户列会形成跨租户插入入口。 */
                @Override
                public boolean ignoreInsert(List<Column> columns, String tenantIdColumn) {
                    getTenantId();
                    if (columns.stream().anyMatch(column -> tenantIdColumn.equalsIgnoreCase(column.getColumnName()))) {
                        throw new TenantAccessDeniedException("受保护租户表禁止显式写入租户字段");
                    }
                    return false;
                }
            }) {
                /** 禁止直接修改受保护表的租户归属。 */
                @Override
                protected void processUpdate(Update update, int index, String sql, Object context) {
                    if (update.getTable() != null
                            && protectedTables.contains(update.getTable().getName().toLowerCase(Locale.ROOT))
                            && update.getUpdateSets().stream()
                            .flatMap(updateSet -> updateSet.getColumns().stream())
                            .anyMatch(column -> "tenant_id".equalsIgnoreCase(column.getColumnName()))) {
                        throw new TenantAccessDeniedException("受保护租户表禁止修改租户字段");
                    }
                    super.processUpdate(update, index, sql, context);
                }
            });
        }
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 防全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

    /**
     * 自动填充处理器
     */
    @Bean
    public MetaObjectHandler autoFillFieldHandler(AuthenticationContext authenticationContext) {
        return new AutoFillFieldHandler(authenticationContext);
    }
}
