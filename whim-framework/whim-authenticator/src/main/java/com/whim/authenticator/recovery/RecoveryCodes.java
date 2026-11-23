package com.whim.authenticator.recovery;

import com.whim.core.utils.IdUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 签发一次性恢复码并核验其持久化摘要，使用成功后由接入方撤销绑定。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RecoveryCodes {
    /**
     * 生成只在绑定期间展示的恢复码。
     *
     * @return 新的恢复码
     */
    public static String generate() {
        return IdUtils.uuid32();
    }

    /**
     * 计算恢复码的持久化摘要，兼容用户以大写或小写输入。
     *
     * @param code 恢复码原文
     * @return 十六进制摘要
     */
    public static String digest(String code) {
        return DigestUtils.sha256Hex(code.toLowerCase(Locale.ROOT));
    }

    /**
     * 识别并核验恢复码，符合恢复码长度但摘要不匹配时拒绝验证。
     *
     * @param code 用户输入的动态码或恢复码
     * @param expectedDigest 绑定时保存的恢复码摘要
     * @return 是否已通过恢复码验证
     */
    public static boolean verifyIfRecoveryCode(String code, String expectedDigest) {
        if (code.length() != 32) {
            return false;
        }
        if (!MessageDigest.isEqual(digest(code).getBytes(StandardCharsets.US_ASCII),
                expectedDigest.getBytes(StandardCharsets.US_ASCII))) {
            throw new IllegalArgumentException("恢复码不正确");
        }
        return true;
    }
}
