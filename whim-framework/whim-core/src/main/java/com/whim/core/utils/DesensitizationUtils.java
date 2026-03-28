package com.whim.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @author Jince
 * @date 2026/03/26
 * @description 脱敏工具类，提供通用脱敏与内置类型脱敏能力，便于后续扩展到 JSON 序列化场景。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DesensitizationUtils {

    /**
     * 默认脱敏文本。
     */
    public static final String DEFAULT_MASK_TEXT = "*";

    /**
     * 按保留前后位数执行脱敏。
     *
     * @param value 原始值
     * @param prefixKeep 保留前缀长度
     * @param suffixKeep 保留后缀长度
     * @return 脱敏后的字符串
     */
    public static String mask(String value, int prefixKeep, int suffixKeep) {
        return mask(value, prefixKeep, suffixKeep, DEFAULT_MASK_TEXT);
    }

    /**
     * 按保留前后位数与脱敏文本执行脱敏。
     *
     * @param value 原始值
     * @param prefixKeep 保留前缀长度
     * @param suffixKeep 保留后缀长度
     * @param maskText 脱敏文本
     * @return 脱敏后的字符串
     */
    public static String mask(String value, int prefixKeep, int suffixKeep, String maskText) {
        return mask(value, prefixKeep, suffixKeep, maskText, true);
    }

    /**
     * 按保留前后位数与脱敏文本执行脱敏。
     *
     * @param value 原始值
     * @param prefixKeep 保留前缀长度
     * @param suffixKeep 保留后缀长度
     * @param maskText 脱敏文本
     * @param repeatMaskText 是否按隐藏长度重复脱敏文本
     * @return 脱敏后的字符串
     */
    public static String mask(String value, int prefixKeep, int suffixKeep, String maskText, boolean repeatMaskText) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        validateKeepLength(prefixKeep, suffixKeep);
        String normalizedMaskText = normalizeMaskText(maskText);
        if (prefixKeep + suffixKeep >= value.length()) {
            return value;
        }
        String prefix = value.substring(0, prefixKeep);
        String suffix = value.substring(value.length() - suffixKeep);
        String hiddenText = buildMaskText(value.length() - prefixKeep - suffixKeep, normalizedMaskText, repeatMaskText);
        return prefix + hiddenText + suffix;
    }

    /**
     * 按内置脱敏类型执行脱敏。
     *
     * @param value 原始值
     * @param type 脱敏类型
     * @return 脱敏后的字符串
     */
    public static String mask(String value, DesensitizationType type) {
        return mask(value, type, DEFAULT_MASK_TEXT);
    }

    /**
     * 按内置脱敏类型与自定义脱敏文本执行脱敏。
     *
     * @param value 原始值
     * @param type 脱敏类型
     * @param maskText 脱敏文本
     * @return 脱敏后的字符串
     */
    public static String mask(String value, DesensitizationType type, String maskText) {
        Objects.requireNonNull(type, "参数[type]不能为空");
        return switch (type) {
            case PHONE -> mask(value, 3, 4, maskText);
            case ID_CARD -> mask(value, 6, 4, maskText);
            case EMAIL -> {
                if (value == null || value.isEmpty()) {
                    yield value;
                }
                int atIndex = value.indexOf('@');
                if (atIndex <= 1 || atIndex == value.length() - 1) {
                    yield mask(value, 1, 0, maskText);
                }
                String localPart = value.substring(0, atIndex);
                String domainPart = value.substring(atIndex);
                String hiddenText = buildMaskText(localPart.length() - 1, normalizeMaskText(maskText), true);
                yield localPart.charAt(0) + hiddenText + domainPart;
            }
            case NAME -> {
                if (value == null || value.isEmpty()) {
                    yield value;
                }
                if (value.length() == 1) {
                    yield value;
                }
                yield value.charAt(0) + buildMaskText(value.length() - 1, normalizeMaskText(maskText), true);
            }
            case BANK_CARD -> mask(value, 4, 4, maskText);
        };
    }

    /**
     * 校验保留长度。
     *
     * @param prefixKeep 保留前缀长度
     * @param suffixKeep 保留后缀长度
     */
    private static void validateKeepLength(int prefixKeep, int suffixKeep) {
        if (prefixKeep < 0) {
            throw new IllegalArgumentException("参数[prefixKeep]不能为负数");
        }
        if (suffixKeep < 0) {
            throw new IllegalArgumentException("参数[suffixKeep]不能为负数");
        }
    }

    /**
     * 规范化脱敏文本。
     *
     * @param maskText 脱敏文本
     * @return 规范化后的脱敏文本
     */
    private static String normalizeMaskText(String maskText) {
        if (maskText == null || maskText.isEmpty()) {
            throw new IllegalArgumentException("参数[maskText]不能为空");
        }
        return maskText;
    }

    /**
     * 构建脱敏文本。
     *
     * @param hiddenLength 被脱敏长度
     * @param maskText 脱敏文本
     * @param repeatMaskText 是否按隐藏长度重复脱敏文本
     * @return 脱敏文本
     */
    private static String buildMaskText(int hiddenLength, String maskText, boolean repeatMaskText) {
        if (hiddenLength <= 0) {
            return "";
        }
        return repeatMaskText ? maskText.repeat(hiddenLength) : maskText;
    }

    /**
     * 脱敏类型枚举。
     */
    public enum DesensitizationType {

        /**
         * 手机号。
         */
        PHONE,

        /**
         * 身份证号。
         */
        ID_CARD,

        /**
         * 邮箱。
         */
        EMAIL,

        /**
         * 姓名。
         */
        NAME,

        /**
         * 银行卡号。
         */
        BANK_CARD
    }
}
