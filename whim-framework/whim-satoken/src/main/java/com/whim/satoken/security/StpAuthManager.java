package com.whim.satoken.security;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import com.whim.satoken.constants.AuthUserType;
import lombok.Getter;

import java.util.List;

/**
 * @author jince
 * @date 2026/4/10
 * @description StpLogic 门面类，管理项目中所有的 StpLogic 账号体系
 */
@Getter
public class StpAuthManager {
    /**
     * System 会话对象，管理 System 表所有账号的登录、权限认证
     */
    public static final StpLogic SYSTEM = new StpLogicJwtForSimple(AuthUserType.SYSTEM);

    /**
     * 项目中全部多账号 StpLogic，供 Spring 注册到 Sa-Token；新增账号体系时须在此追加对应常量并在本方法中加入。
     *
     * @return 全部 StpLogic 实例，顺序对框架无影响
     */
    public static List<StpLogic> allRegistered() {
        return List.of(SYSTEM);
    }
}
