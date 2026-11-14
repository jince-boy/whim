package com.whim.satoken.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jince
 * @date 2026/09/22
 * @description Sa-Token 认证会话键常量。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthSessionKeys {

    /**
     * 包含独立管理员身份的登录上下文键；旧版角色授权快照需要重新登录后更新。
     */
    public static final String LOGIN_USER = "userInfo:administratorIdentity:v1";
}
