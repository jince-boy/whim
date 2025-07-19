package com.whim.core.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * @author Jince
 * @date 2024/10/4 00:46
 * @description BCryptUtils 是一个用于处理密码加密和验证的工具类。
 * 该类提供了使用 BCrypt 算法对密码进行加密和验证的方法。
 * BCrypt 是一种安全的哈希算法，具有加盐和自适应加密机制，能够有效抵御暴力破解和字典攻击。
 */
public class BCryptUtils {
    private static final int DEFAULT_COST = 10;

    /**
     * BCrypt编码
     *
     * @param str 字符串
     * @return 编码后的字符串
     */
    public static String encode(String str) {
        return BCrypt.hashpw(str, BCrypt.gensalt(DEFAULT_COST));
    }

    /**
     * @param rawStr     原字符串
     * @param encodedStr 编码后的字符串
     * @return true正确，false不正确
     */
    public static boolean matches(String rawStr, String encodedStr) {
        return BCrypt.checkpw(rawStr, encodedStr);
    }
}
