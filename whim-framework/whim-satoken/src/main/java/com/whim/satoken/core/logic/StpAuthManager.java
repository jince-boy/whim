package com.whim.satoken.core.logic;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import com.whim.satoken.core.common.AccountType;
import lombok.Getter;

/**
 * @author jince
 * @date 2025/3/28 18:45
 * @description StpLogic 门面类，管理项目中所有的 StpLogic 账号体系
 */
@Getter
public class StpAuthManager {
    /**
     * System 会话对象，管理 System 表所有账号的登录、权限认证
     */
    public static final StpLogic SYSTEM = new StpLogicJwtForSimple(AccountType.SYSTEM);
}
