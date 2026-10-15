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
 * @description 为原生数据权限插件补充防绕过和内部元数据只读约束，SQL 改写仍由原生插件执行。
 */
public class DataPermissionInterceptor extends com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor {
    private final Map<String, Boolean> metadataStatements = new ConcurrentHashMap<>();

    /** 使用通用多表权限条件处理器。 */
    public DataPermissionInterceptor() {
        super(new DataPermissionSqlHandler());
    }

    /** 在分页及缓存键生成前过滤查询，无业务范围或合法元数据查询不解析 SQL。 */
    @Override
    public void beforeQuery(Executor executor, MappedStatement statement, Object parameter, RowBounds rowBounds,
                            ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        if (skipDataPermission(statement)) {
            return;
        }
        super.beforeQuery(executor, statement, parameter, rowBounds, resultHandler, boundSql);
    }

    /** 每次写入先检查豁免声明，批处理与语句复用也不能以元数据名义写入。 */
    @Override
    public void beforeUpdate(Executor executor, MappedStatement statement, Object parameter) {
        skipDataPermission(statement);
    }

    /** 复用原生更新和删除过滤，INSERT 与归属变更仍由 checkOwnership 校验。 */
    @Override
    public void beforePrepare(StatementHandler handler, Connection connection, Integer transactionTimeout) {
        MappedStatement statement = PluginUtils.mpStatementHandler(handler).mappedStatement();
        if (skipDataPermission(statement)) {
            return;
        }
        super.beforePrepare(handler, connection, transactionTimeout);
    }

    /** 元数据始终只读；受保护业务禁止通过方法、Mapper 或线程级忽略策略关闭过滤。 */
    private boolean skipDataPermission(MappedStatement statement) {
        if (isMetadataStatement(statement.getId())) {
            if (statement.getSqlCommandType() != SqlCommandType.SELECT) {
                throw new DataAccessDeniedException("授权元数据声明只能用于内部只读查询");
            }
            return true;
        }
        if (!DataPermissionContext.active()) {
            return true;
        }
        if (InterceptorIgnoreHelper.willIgnoreDataPermission(statement.getId())) {
            throw new DataAccessDeniedException("受保护业务操作不能忽略数据权限");
        }
        return false;
    }

    /** 只识别明确标注的 Mapper 方法并缓存结果，不提供整类豁免入口。 */
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
