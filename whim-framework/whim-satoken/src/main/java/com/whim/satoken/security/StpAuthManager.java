package com.whim.satoken.security;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import com.whim.satoken.constants.AuthUserType;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

/**
 * @author Jince
 * @date 2026/04/13
 * @description StpLogic 门面类，统一管理项目中的所有账号体系，并负责解析当前请求所属的认证体系。
 */
@Getter
public class StpAuthManager {
    /**
     * System 会话对象，管理 System 表所有账号的登录、权限认证
     */
    public static final StpLogic SYSTEM = new StpLogicJwtForSimple(AuthUserType.SYSTEM);

    /**
     * 获取项目中全部已注册的账号体系。
     *
     * @return 全部 StpLogic 实例
     */
    private static List<StpLogic> getRegisteredStpLogics() {
        return List.of(SYSTEM);
    }

    /**
     * 获取项目中全部多账号 StpLogic，供 Spring 注册到 Sa-Token。
     * 新增账号体系时，需要在此追加对应常量并加入返回集合。
     *
     * @return 全部 StpLogic 实例，顺序对框架无影响
     */
    public static List<StpLogic> allRegistered() {
        return getRegisteredStpLogics();
    }

    /**
     * 获取当前请求所属的账号体系。
     * 按照 Sa-Token 多账号体系的推荐方式，逐个判断各体系是否已登录，命中的体系即为当前认证体系。
     *
     * @return 当前请求所属的 StpLogic；如果当前请求未登录任何体系，则返回 null
     */
    public static StpLogic getCurrentStpLogic() {
        return getRegisteredStpLogics().stream()
                .filter(Objects::nonNull)
                .filter(StpLogic::isLogin)
                .findFirst()
                .orElse(null);
    }

    /**
     * 判断当前请求是否已登录任意账号体系。
     *
     * @return true 表示当前请求已登录
     */
    public static boolean isLogin() {
        return Objects.nonNull(getCurrentStpLogic());
    }
}
