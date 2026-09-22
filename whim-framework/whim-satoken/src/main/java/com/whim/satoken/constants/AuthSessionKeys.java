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
     * 登录用户上下文会话键
     */
    public static final String LOGIN_USER = "userInfo";
}
