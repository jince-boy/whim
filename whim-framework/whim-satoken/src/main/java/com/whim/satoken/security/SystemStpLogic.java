package com.whim.satoken.security;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.session.SaSession;
import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.auth.model.UserInfo;
import com.whim.satoken.constants.AuthSessionKeys;

import java.util.Objects;

/**
 * @author Jince
 * @date 2026/11/14
 * @description 系统账号授权，超级管理员身份默认拥有全部功能权限。
 */
public class SystemStpLogic extends StpLogicJwtForSimple {

    /**
     * 初始化系统账号的JWT认证逻辑。
     */
    public SystemStpLogic() {
        super(AuthUserType.SYSTEM);
    }

    /**
     * 校验指定账号的操作权限，超级管理员无需关联角色或菜单。
     *
     * @param loginId 账号ID
     * @param permission 操作权限码
     * @return 是否拥有操作权限
     */
    @Override
    public boolean hasPermission(Object loginId, String permission) {
        return isSuperAdministrator(loginId) || super.hasPermission(loginId, permission);
    }

    /**
     * 校验当前账号是否拥有全部指定操作权限。
     *
     * @param permissionArray 操作权限码集合
     */
    @Override
    public void checkPermissionAnd(String... permissionArray) {
        if (!isSuperAdministrator(getLoginId())) {
            super.checkPermissionAnd(permissionArray);
        }
    }

    /**
     * 校验当前账号是否拥有任意一个指定操作权限。
     *
     * @param permissionArray 操作权限码集合
     */
    @Override
    public void checkPermissionOr(String... permissionArray) {
        if (!isSuperAdministrator(getLoginId())) {
            super.checkPermissionOr(permissionArray);
        }
    }

    /**
     * 从当前令牌快照读取与目标账号一致的超级管理员身份。
     *
     * @param loginId 待校验账号ID
     * @return 当前令牌是否属于该超级管理员账号
     */
    private boolean isSuperAdministrator(Object loginId) {
        SaSession session = getTokenSession(false);
        if (session == null) {
            return false;
        }
        // 身份只信任服务端登录快照，不能用当前管理员的身份为其他账号代为放行。
        Object value = session.get(AuthSessionKeys.LOGIN_USER);
        return value instanceof UserInfo userInfo && userInfo.isSuperAdministrator()
                && Objects.equals(String.valueOf(userInfo.getUserId()), String.valueOf(loginId));
    }
}
