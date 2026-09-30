package com.whim.mybatisplus.permission;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.whim.core.exception.DataAccessDeniedException;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 对受注解保护的查询和写入执行过滤，禁止忽略拦截器绕过声明。
 */
public class DataPermissionInterceptor extends com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor {
    private final Map<String, Boolean> metadataStatements = new ConcurrentHashMap<>();
    /** 使用通用多表权限条件处理器。 */
    public DataPermissionInterceptor() {
        super(new DataPermissionSqlHandler());
    }

    /** 在分页及计数之前处理查询，未进入受保护业务方法时不解析 SQL。 */
    @Override
    public void beforeQuery(Executor executor, MappedStatement statement, Object parameter, RowBounds rowBounds,
                            ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        if (!DataPermissionContext.active()) {
            return;
        }
        if (isMetadataStatement(statement.getId())) {
            return;
        }
        if (InterceptorIgnoreHelper.willIgnoreDataPermission(statement.getId())) {
            throw new DataAccessDeniedException("受保护业务操作不能忽略数据权限");
        }
        super.beforeQuery(executor, statement, parameter, rowBounds, resultHandler, boundSql);
    }

    /** 在 SQL 执行前处理更新和删除，归属字段新增与变更由业务服务同步校验。 */
    @Override
    public void beforePrepare(StatementHandler handler, Connection connection, Integer transactionTimeout) {
        if (!DataPermissionContext.active()) {
            return;
        }
        MappedStatement statement = PluginUtils.mpStatementHandler(handler).mappedStatement();
        if (isMetadataStatement(statement.getId())) {
            if (statement.getSqlCommandType() != SqlCommandType.SELECT) {
                throw new DataAccessDeniedException("授权元数据声明只能用于内部只读查询");
            }
            return;
        }
        if (InterceptorIgnoreHelper.willIgnoreDataPermission(statement.getId())) {
            throw new DataAccessDeniedException("受保护业务操作不能忽略数据权限");
        }
        super.beforePrepare(handler, connection, transactionTimeout);
    }

    /** 仅识别开发者明确标记的 Mapper 方法，普通业务 SQL 没有忽略入口。 */
    private boolean isMetadataStatement(String statementId) {
        return metadataStatements.computeIfAbsent(statementId, identifier -> {
            int separator = identifier.lastIndexOf('.');
            if (separator < 0) {
                return false;
            }
            try {
                Class<?> mapper = Class.forName(identifier.substring(0, separator));
                String methodName = identifier.substring(separator + 1);
                return Arrays.stream(mapper.getMethods()).anyMatch(method -> method.getName().equals(methodName)
                        && method.isAnnotationPresent(DataPermissionMetadata.class));
            } catch (ClassNotFoundException exception) {
                return false;
            }
        });
    }
}
