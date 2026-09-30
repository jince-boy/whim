package com.whim.mybatisplus.permission;

import com.whim.core.exception.DataAccessDeniedException;
import com.whim.core.permission.DataPermissionProvider;
import com.whim.core.permission.DataPermissionScope;
import com.whim.mybatisplus.annotation.DataPermission;
import com.whim.mybatisplus.annotation.DataPermissionTable;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Deque;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 在业务调用前解析精确操作授权，保证异常退出与嵌套调用清理上下文。
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@RequiredArgsConstructor
public class DataPermissionAspect {
    private final ObjectProvider<DataPermissionProvider> permissionProviders;

    /** 校验声明并建立范围栈；权限元数据解析独立于外层业务查询过滤。 */
    @Around("@annotation(permission)")
    public Object authorize(ProceedingJoinPoint invocation, DataPermission permission) throws Throwable {
        if (permission.permission().isBlank() || permission.tables().length == 0) {
            throw new DataAccessDeniedException("数据权限注解缺少操作权限或业务表映射");
        }
        for (DataPermissionTable table : permission.tables()) {
            validateIdentifier(table.name(), false);
            validateIdentifier(table.alias(), true);
            validateIdentifier(table.departmentColumn(), true);
            validateIdentifier(table.userColumn(), true);
            if (table.departmentColumn().isEmpty() && table.userColumn().isEmpty()) {
                throw new DataAccessDeniedException("数据权限表映射缺少业务归属列");
            }
        }
        DataPermissionProvider provider = permissionProviders.getIfAvailable();
        if (provider == null) {
            throw new DataAccessDeniedException("当前应用未提供数据权限解析器");
        }
        Deque<DataPermissionContext.Frame> existing = DataPermissionContext.FRAMES.get();
        DataPermissionScope scope;
        DataPermissionContext.FRAMES.remove();
        try {
            scope = provider.resolve(permission.permission());
        } finally {
            if (existing != null) {
                DataPermissionContext.FRAMES.set(existing);
            }
        }
        if (scope == null || scope.getUserId() == null) {
            throw new DataAccessDeniedException("数据权限解析结果缺少有效身份");
        }
        if (!scope.isDataProtected()) {
            throw new DataAccessDeniedException("注解对应的操作未登记为数据权限操作");
        }
        Deque<DataPermissionContext.Frame> frames = DataPermissionContext.frames();
        frames.push(new DataPermissionContext.Frame(scope, permission.tables()));
        try {
            return invocation.proceed();
        } finally {
            frames.pop();
            if (frames.isEmpty()) {
                DataPermissionContext.FRAMES.remove();
            }
        }
    }

    /** 仅允许开发者声明普通表名、别名和列名，避免将表达式当作标识符。 */
    private void validateIdentifier(String identifier, boolean optional) {
        if ((optional && identifier.isEmpty()) || identifier.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            return;
        }
        throw new DataAccessDeniedException("数据权限表名或列名配置无效");
    }
}
