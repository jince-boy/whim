package com.whim.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Jince
 * @date 2024/10/4 00:46
 * @description BCrypt 密码工具类，提供密码加密与校验能力。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BCryptUtils {

    /**
     * BCrypt strength，值越大计算成本越高。
     */
    private static final int STRENGTH = 10;

    /**
     * 共享的密码编码器实例。
     */
    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(STRENGTH);

    /**
     * 对明文密码进行 BCrypt 哈希编码。
     *
     * @param rawPassword 明文密码
     * @return 编码后的密码字符串
     */
    public static String encode(String rawPassword) {
        return PASSWORD_ENCODER.encode(rawPassword);
    }

    /**
     * 校验明文密码是否与已编码密码匹配。
     *
     * @param rawPassword 明文密码
     * @param encodedPassword 已编码密码
     * @return 匹配返回 {@code true}，否则返回 {@code false}
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return rawPassword != null
                && encodedPassword != null
                && PASSWORD_ENCODER.matches(rawPassword, encodedPassword);
    }
}
