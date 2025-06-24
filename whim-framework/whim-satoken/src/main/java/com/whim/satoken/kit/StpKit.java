package com.whim.satoken.kit;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;

/**
 * @author jince
 * date: 2025/3/28 18:45
 * description: Kit模式 StpLogic 门面类，管理项目中所有的 StpLogic 账号体系
 */
public class StpKit {
    /**
     * 多账号认证类型（内置常量，避免魔法字符串）
     */
    public static final class AccountType {
        private AccountType() {
        }

        public static final String SYSTEM = "system";
    }

    /**
     * 默认原生会话对象
     */
    public static final StpLogic DEFAULT = StpUtil.stpLogic;

    /**
     * System 会话对象，管理 System 表所有账号的登录、权限认证
     */
    public static final StpLogic SYSTEM = new StpLogicJwtForSimple(AccountType.SYSTEM);
    /**
     * XX 会话对象，（项目中有多少套账号表，就声明几个 StpLogic 会话对象）
     */
    public static final StpLogic XXX = new StpLogicJwtForSimple("xx");
}
