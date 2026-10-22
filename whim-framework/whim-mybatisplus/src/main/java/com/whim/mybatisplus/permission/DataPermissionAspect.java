package com.whim.mybatisplus.permission;

import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.model.RoleInfo;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.exception.DataAccessDeniedException;
import com.whim.mybatisplus.annotation.DataPermission;
import com.whim.mybatisplus.annotation.DataPermissionTable;
import com.whim.mybatisplus.model.enums.DataScopeType;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 从登录上下文合并当前操作的角色范围，保证异常退出与嵌套调用清理上下文。
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@RequiredArgsConstructor
public class DataPermissionAspect {
    private final AuthenticationContext authenticationContext;

    /** 校验表映射并合并登录快照中的角色范围，全程不查询数据库。 */
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
        UserInfo user = authenticationContext.getCurrentUserInfo();
        boolean all = authenticationContext.isSuperAdministrator();
        boolean self = false;
        Set<Long> departmentIds = new LinkedHashSet<>();
        if (!all) {
            Set<Long> roleIds = user.getPermissionRoleIds().getOrDefault(permission.permission(), Set.of());
            List<RoleInfo> roles = user.getRoleInfoList().stream()
                    .filter(role -> roleIds.contains(role.getRoleId())).toList();
            if (roles.isEmpty()) {
                throw new DataAccessDeniedException("登录上下文缺少本次操作的角色范围，请重新登录");
            }
            for (RoleInfo role : roles) {
                switch (DataScopeType.fromCode(role.getDataScope())) {
                    case ALL -> all = true;
                    case SELF -> self = true;
                    case CUSTOM_DEPARTMENTS, OWN_DEPARTMENT, DEPARTMENT_AND_DESCENDANTS -> {
                        Set<Long> roleDepartments = user.getRoleDepartmentIds().get(role.getRoleId());
                        if (roleDepartments == null) {
                            throw new DataAccessDeniedException("登录上下文缺少角色部门范围，请重新登录");
                        }
                        departmentIds.addAll(roleDepartments);
                    }
                }
                if (all) {
                    break;
                }
            }
        }
        Deque<DataPermissionContext.Frame> frames = DataPermissionContext.frames();
        frames.push(new DataPermissionContext.Frame(user.getUserId(), all, self,
                departmentIds, permission.tables()));
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
