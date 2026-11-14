package com.whim.satoken.context;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.exception.UnauthenticatedException;
import com.whim.satoken.constants.AuthSessionKeys;
import com.whim.satoken.security.StpAuthManager;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Objects;

/**
 * @author Jince
 * @date 2026/04/13
 * @description 基于 Sa-Token 的认证上下文实现。
 * 通过 {@link StpAuthManager} 动态识别当前请求所属的账号体系，避免与具体体系常量耦合。
 */
public class AuthContext implements AuthenticationContext {

    /**
     * 获取当前登录用户信息。
     *
     * @return 当前登录用户信息
     */
    @Override
    public UserInfo getCurrentUserInfo() {
        UserInfo userInfo = getCurrentUserInfoOrNull();
        if (Objects.isNull(userInfo)) {
            throw new UnauthenticatedException("登录授权信息已失效，请重新登录");
        }
        return userInfo;
    }

    /**
     * 判断当前请求是否已完成登录认证，后台任务没有登录身份。
     *
     * @return true 表示当前请求已登录
     */
    @Override
    public boolean isLogin() {
        // 定时清理等后台任务也会触发审计填充，不应访问不存在的 Servlet 上下文。
        return RequestContextHolder.getRequestAttributes() != null && StpAuthManager.isLogin();
    }

    /**
     * 获取当前请求所属的 Sa-Token 账号体系。
     *
     * @return 当前请求所属的 Sa-Token 账号体系
     */
    private StpLogic getRequiredCurrentStpLogic() {
        StpLogic stpLogic = StpAuthManager.getCurrentStpLogic();
        if (Objects.isNull(stpLogic)) {
            throw new IllegalStateException("当前请求未登录，无法获取认证上下文");
        }
        return stpLogic;
    }

    /**
     * 获取当前登录用户信息，缺失时返回 null。
     *
     * @return 当前登录用户信息
     */
    private UserInfo getCurrentUserInfoOrNull() {
        if (!isLogin()) {
            return null;
        }
        SaSession tokenSession = getRequiredCurrentStpLogic().getTokenSession(false);
        if (Objects.isNull(tokenSession)) {
            return null;
        }
        Object userInfo = tokenSession.get(AuthSessionKeys.LOGIN_USER);
        if (userInfo instanceof UserInfo currentUserInfo) {
            return currentUserInfo;
        }
        return null;
    }
}
