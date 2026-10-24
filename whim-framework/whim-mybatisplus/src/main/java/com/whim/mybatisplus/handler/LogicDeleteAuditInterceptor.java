package com.whim.mybatisplus.handler;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 在逻辑删除执行期间向审计填充器传递操作类型。
 */
@Intercepts(@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}))
public class LogicDeleteAuditInterceptor implements Interceptor {
    private static final ScopedValue<Boolean> LOGIC_DELETE = ScopedValue.newInstance();
    private static final Set<String> DELETE_METHODS = Set.of("deleteById", "deleteByIds", "deleteBatchIds", "delete");

    /** 逻辑删除以 UPDATE 执行，删除标识由 SQL 赋值，不能从填充前的实体判断操作类型。 */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement statement = (MappedStatement) invocation.getArgs()[0];
        String statementId = statement.getId();
        String methodName = statementId.substring(statementId.lastIndexOf('.') + 1);
        boolean logicDelete = statement.getSqlCommandType() == SqlCommandType.UPDATE
                && DELETE_METHODS.contains(methodName);
        return ScopedValue.where(LOGIC_DELETE, logicDelete).call(invocation::proceed);
    }

    /** 获取当前 Mapper 写入是否为逻辑删除，嵌套执行结束后自动恢复外层值。 */
    static boolean isLogicDelete() {
        return LOGIC_DELETE.orElse(false);
    }
}
