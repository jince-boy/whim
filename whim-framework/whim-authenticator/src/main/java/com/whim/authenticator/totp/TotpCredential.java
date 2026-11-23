package com.whim.authenticator.totp;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 首次动态码验证成功后需要原子保存的凭据。
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class TotpCredential {
    private final String encryptedSecret;
    private final String recoveryHash;
    private final long lastStep;
}
