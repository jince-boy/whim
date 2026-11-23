package com.whim.authenticator.totp;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 验证器绑定时需要向本人展示一次的资料。
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class TotpEnrollment {
    private final String setupId;
    private final String secret;
    private final String recoveryCode;
}
