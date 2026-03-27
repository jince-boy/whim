package com.whim.json.annotation;

/**
 * @author Jince
 * @date 2026/03/27
 * @description 脱敏类型定义。
 */
public enum DesensitizationType {
    PHONE,
    EMAIL,
    ID_CARD,
    NAME,
    BANK_CARD,
    CUSTOM;

    /**
     * 对原始文本执行脱敏处理。
     *
     * @param value 原始文本
     * @param prefixKeep 自定义前缀保留长度
     * @param suffixKeep 自定义后缀保留长度
     * @param maskChar 脱敏字符
     * @return 脱敏后的文本
     */
    public String mask(String value, int prefixKeep, int suffixKeep, char maskChar) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return switch (this) {
            case PHONE -> maskByKeep(value, 3, 4, maskChar);
            case EMAIL -> maskEmail(value, maskChar);
            case ID_CARD, BANK_CARD -> maskByKeep(value, 6, 4, maskChar);
            case NAME -> maskName(value, maskChar);
            case CUSTOM -> maskByKeep(value, prefixKeep, suffixKeep, maskChar);
        };
    }

    /**
     * 按保留前后缀规则执行脱敏。
     *
     * @param value 原始文本
     * @param prefixKeep 前缀保留长度
     * @param suffixKeep 后缀保留长度
     * @param maskChar 脱敏字符
     * @return 脱敏后的文本
     */
    private static String maskByKeep(String value, int prefixKeep, int suffixKeep, char maskChar) {
        var safePrefixKeep = Math.max(prefixKeep, 0);
        var safeSuffixKeep = Math.max(suffixKeep, 0);
        if (value.length() <= safePrefixKeep + safeSuffixKeep) {
            return repeat(maskChar, value.length());
        }
        return value.substring(0, safePrefixKeep)
                + repeat(maskChar, value.length() - safePrefixKeep - safeSuffixKeep)
                + value.substring(value.length() - safeSuffixKeep);
    }

    /**
     * 对邮箱地址执行脱敏。
     *
     * @param value 原始邮箱
     * @param maskChar 脱敏字符
     * @return 脱敏后的邮箱
     */
    private static String maskEmail(String value, char maskChar) {
        var atIndex = value.indexOf('@');
        if (atIndex <= 1) {
            return maskByKeep(value, 1, 0, maskChar);
        }
        var localPart = value.substring(0, atIndex);
        var domainPart = value.substring(atIndex);
        return maskByKeep(localPart, 1, 0, maskChar) + domainPart;
    }

    /**
     * 对姓名执行脱敏。
     *
     * @param value 原始姓名
     * @param maskChar 脱敏字符
     * @return 脱敏后的姓名
     */
    private static String maskName(String value, char maskChar) {
        if (value.length() == 1) {
            return value;
        }
        if (value.length() == 2) {
            return value.charAt(0) + String.valueOf(maskChar);
        }
        return value.charAt(0) + repeat(maskChar, value.length() - 2) + value.charAt(value.length() - 1);
    }

    /**
     * 构建重复脱敏字符文本。
     *
     * @param maskChar 脱敏字符
     * @param count 重复次数
     * @return 脱敏字符文本
     */
    private static String repeat(char maskChar, int count) {
        return String.valueOf(maskChar).repeat(Math.max(count, 0));
    }
}
