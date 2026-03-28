package com.whim.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;

/**
 * @author Jince
 * @date 2026/03/26
 * @description 随机字符串工具类，基于安全随机数生成器提供可配置长度、大小写字母、数字及扩展字符池的随机字符串生成能力。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RandomStringUtils {

    /**
     * 大写字母字符池。
     */
    public static final String UPPERCASE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 小写字母字符池。
     */
    public static final String LOWERCASE_CHARACTERS = "abcdefghijklmnopqrstuvwxyz";

    /**
     * 数字字符池。
     */
    public static final String DIGIT_CHARACTERS = "0123456789";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 生成默认随机字符串。
     *
     * @param length 生成长度
     * @return 包含大小写字母和数字的随机字符串
     */
    public static String random(int length) {
        return random(length, true, true, true);
    }

    /**
     * 按字符类型开关生成随机字符串。
     *
     * @param length           生成长度
     * @param includeUppercase 是否包含大写字母
     * @param includeLowercase 是否包含小写字母
     * @param includeDigits    是否包含数字
     * @return 随机字符串
     */
    public static String random(int length, boolean includeUppercase, boolean includeLowercase, boolean includeDigits) {
        return random(length, includeUppercase, includeLowercase, includeDigits, "");
    }

    /**
     * 按字符类型开关和附加字符池生成随机字符串。
     *
     * @param length           生成长度
     * @param includeUppercase 是否包含大写字母
     * @param includeLowercase 是否包含小写字母
     * @param includeDigits    是否包含数字
     * @param extraCharacters  额外字符池，允许为空白
     * @return 随机字符串
     */
    public static String random(
            int length,
            boolean includeUppercase,
            boolean includeLowercase,
            boolean includeDigits,
            String extraCharacters
    ) {
        validateLength(length);
        return generate(length, buildCharacterPool(includeUppercase, includeLowercase, includeDigits, extraCharacters));
    }

    /**
     * 按指定字符池生成随机字符串。
     *
     * @param length     生成长度
     * @param characters 自定义字符池
     * @return 随机字符串
     */
    public static String randomByCharacters(int length, String characters) {
        validateLength(length);
        return generate(length, requireHasText(characters, "characters"));
    }

    /**
     * 生成仅包含字母的随机字符串。
     *
     * @param length 生成长度
     * @return 字母随机字符串
     */
    public static String randomAlphabetic(int length) {
        return random(length, true, true, false);
    }

    /**
     * 生成仅包含大写字母的随机字符串。
     *
     * @param length 生成长度
     * @return 大写字母随机字符串
     */
    public static String randomUpperCase(int length) {
        return random(length, true, false, false);
    }

    /**
     * 生成仅包含小写字母的随机字符串。
     *
     * @param length 生成长度
     * @return 小写字母随机字符串
     */
    public static String randomLowerCase(int length) {
        return random(length, false, true, false);
    }

    /**
     * 生成仅包含数字的随机字符串。
     *
     * @param length 生成长度
     * @return 数字随机字符串
     */
    public static String randomNumeric(int length) {
        return random(length, false, false, true);
    }

    /**
     * 生成功能明确的字母数字随机字符串。
     *
     * @param length 生成长度
     * @return 字母数字随机字符串
     */
    public static String randomAlphaNumeric(int length) {
        return random(length, true, true, true);
    }

    /**
     * 构建实际使用的字符池。
     *
     * @param includeUppercase 是否包含大写字母
     * @param includeLowercase 是否包含小写字母
     * @param includeDigits    是否包含数字
     * @param extraCharacters  额外字符池
     * @return 合并后的字符池
     */
    private static String buildCharacterPool(
            boolean includeUppercase,
            boolean includeLowercase,
            boolean includeDigits,
            String extraCharacters
    ) {
        StringBuilder builder = new StringBuilder();
        if (includeUppercase) {
            builder.append(UPPERCASE_CHARACTERS);
        }
        if (includeLowercase) {
            builder.append(LOWERCASE_CHARACTERS);
        }
        if (includeDigits) {
            builder.append(DIGIT_CHARACTERS);
        }
        if (!isBlank(extraCharacters)) {
            builder.append(extraCharacters);
        }
        if (builder.isEmpty()) {
            throw new IllegalArgumentException("至少需要启用一种字符来源");
        }
        return builder.toString();
    }

    /**
     * 根据字符池生成随机字符串。
     *
     * @param length        生成长度
     * @param characterPool 字符池
     * @return 随机字符串
     */
    private static String generate(int length, String characterPool) {
        StringBuilder builder = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            int randomIndex = SECURE_RANDOM.nextInt(characterPool.length());
            builder.append(characterPool.charAt(randomIndex));
        }
        return builder.toString();
    }

    /**
     * 校验生成长度是否合法。
     *
     * @param length 生成长度
     */
    private static void validateLength(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("参数[length]不能为负数");
        }
    }

    /**
     * 校验字符串必须包含有效文本。
     *
     * @param value     待校验值
     * @param fieldName 字段名称
     * @return 原始字符串
     */
    private static String requireHasText(String value, String fieldName) {
        if (isBlank(value)) {
            throw new IllegalArgumentException("参数[%s]不能为空白".formatted(fieldName));
        }
        return value;
    }

    /**
     * 判断字符串是否为空白。
     *
     * @param value 待判断值
     * @return 是否为空白
     */
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
