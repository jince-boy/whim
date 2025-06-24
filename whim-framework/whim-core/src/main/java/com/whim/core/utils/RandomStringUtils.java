package com.whim.core.utils;

import java.security.SecureRandom;

/**
 * @author Jince
 * date: 2024/10/5 00:16
 * description: 随机字符串生成工具类
 * 提供生成包含大写字母、小写字母和数字的随机字符串的方法。
 */
public class RandomStringUtils {
    private static final String LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * 生成指定的随机字符串
     *
     * @param includeLowercase 是否包含小写字母
     * @param includeUppercase 是否包含大写字母
     * @param includeNumbers   是否包含数字
     * @param length           生成的长度
     * @return 随机字符串
     * @throws IllegalArgumentException 如果 length 小于等于 0
     */
    public static String generateRandomString(boolean includeLowercase, boolean includeUppercase, boolean includeNumbers, int length) {
        validateLength(length);

        StringBuilder characters = new StringBuilder();
        if (includeLowercase) {
            characters.append(LOWERCASE_LETTERS);
        }
        if (includeUppercase) {
            characters.append(UPPERCASE_LETTERS);
        }
        if (includeNumbers) {
            characters.append(NUMBERS);
        }
        if (characters.isEmpty()) {
            // 默认包含大写英文和数字
            characters.append(UPPERCASE_LETTERS).append(NUMBERS);
        }

        return generateRandomStringFromCharacters(characters.toString(), length);
    }

    /**
     * 生成默认长度为4的随机字符串，包含大写字母和数字
     *
     * @return 随机字符串
     */
    public static String generateDefaultRandomString() {
        return generateRandomString(true, false, true, 4);
    }

    /**
     * 根据给定的字符集和长度生成随机字符串
     *
     * @param characters 字符集
     * @param length     生成的长度
     * @return 随机字符串
     */
    private static String generateRandomStringFromCharacters(String characters, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(characters.length());
            sb.append(characters.charAt(randomIndex));
        }
        return sb.toString();
    }

    /**
     * 验证生成字符串的长度是否有效
     *
     * @param length 要验证的长度
     * @throws IllegalArgumentException 如果 length 小于等于 0
     */
    private static void validateLength(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be a positive number.");
        }
    }
}