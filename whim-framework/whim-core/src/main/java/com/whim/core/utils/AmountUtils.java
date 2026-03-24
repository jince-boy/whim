package com.whim.core.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * @author Jince
 * @date 2026/3/24
 * @description 金额工具类，提供金额换算、格式处理与中文大写转换能力。
 */
public final class AmountUtils {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final String[] FRACTION_UNITS = {"角", "分"};
    private static final String[] DIGITS = {"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖"};
    private static final String[][] INTEGER_UNITS = {{"元", "万", "亿"}, {"", "拾", "佰", "仟"}};

    private AmountUtils() {
    }

    /**
     * 返回非空金额。
     *
     * @param amount 原金额
     * @return 非空金额
     */
    public static BigDecimal nullToZero(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    /**
     * 判断金额是否为零。
     *
     * @param amount 金额
     * @return 是否为零
     */
    public static boolean isZero(BigDecimal amount) {
        return nullToZero(amount).compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 判断金额是否大于零。
     *
     * @param amount 金额
     * @return 是否大于零
     */
    public static boolean isPositive(BigDecimal amount) {
        return nullToZero(amount).compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 判断金额是否小于零。
     *
     * @param amount 金额
     * @return 是否小于零
     */
    public static boolean isNegative(BigDecimal amount) {
        return nullToZero(amount).compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 按两位小数进行标准化处理。
     *
     * @param amount 金额
     * @return 标准化后的金额
     */
    public static BigDecimal scale(BigDecimal amount) {
        return scale(amount, 2, RoundingMode.HALF_UP);
    }

    /**
     * 按指定精度进行标准化处理。
     *
     * @param amount 金额
     * @param scale 小数位数
     * @param roundingMode 舍入方式
     * @return 标准化后的金额
     */
    public static BigDecimal scale(BigDecimal amount, int scale, RoundingMode roundingMode) {
        return nullToZero(amount).setScale(scale, roundingMode);
    }

    /**
     * 将元转换为分。
     *
     * @param amount 金额
     * @return 分
     */
    public static long toCent(BigDecimal amount) {
        return scale(amount).movePointRight(2).longValueExact();
    }

    /**
     * 将分转换为元。
     *
     * @param cent 分
     * @return 元
     */
    public static BigDecimal fromCent(long cent) {
        return BigDecimal.valueOf(cent).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }

    /**
     * 将金额转换为普通字符串。
     *
     * @param amount 金额
     * @return 金额字符串
     */
    public static String toPlainString(BigDecimal amount) {
        return scale(amount).toPlainString();
    }

    /**
     * 将金额转换为去尾零字符串。
     *
     * @param amount 金额
     * @return 金额字符串
     */
    public static String toTrimmedString(BigDecimal amount) {
        BigDecimal normalizedAmount = nullToZero(amount).stripTrailingZeros();
        return normalizedAmount.scale() < 0 ? normalizedAmount.setScale(0).toPlainString() : normalizedAmount.toPlainString();
    }

    /**
     * 将金额转换为中文大写。
     *
     * @param amount 金额
     * @return 中文大写金额
     */
    public static String toChineseUppercase(BigDecimal amount) {
        if (amount == null) {
            return null;
        }

        BigDecimal normalizedAmount = scale(amount);
        BigDecimal absoluteAmount = normalizedAmount.abs();
        long centValue = absoluteAmount.movePointRight(2).longValue();
        long integerPart = centValue / 100;
        int fractionPart = (int) (centValue % 100);
        String prefix = normalizedAmount.signum() < 0 ? "负" : "";

        StringBuilder result = new StringBuilder();
        if (fractionPart == 0) {
            result.append("整");
        } else {
            int jiao = fractionPart / 10;
            int fen = fractionPart % 10;
            if (jiao > 0) {
                result.append(DIGITS[jiao]).append(FRACTION_UNITS[0]);
            }
            if (fen > 0) {
                if (jiao == 0 && integerPart > 0) {
                    result.append("零");
                }
                result.append(DIGITS[fen]).append(FRACTION_UNITS[1]);
            }
        }

        if (integerPart == 0) {
            return prefix + ("整".contentEquals(result) ? "零元整" : "零元" + result);
        }

        StringBuilder integerBuilder = new StringBuilder();
        for (int i = 0; i < INTEGER_UNITS[0].length && integerPart > 0; i++) {
            StringBuilder section = new StringBuilder();
            for (int j = 0; j < INTEGER_UNITS[1].length && integerPart > 0; j++) {
                int currentDigit = (int) (integerPart % 10);
                if (currentDigit != 0) {
                    section.insert(0, DIGITS[currentDigit] + INTEGER_UNITS[1][j]);
                } else if (!section.isEmpty() && section.charAt(0) != '零') {
                    section.insert(0, "零");
                }
                integerPart /= 10;
            }
            String normalizedSection = normalizeChineseInteger(section.toString());
            if (!normalizedSection.isEmpty()) {
                integerBuilder.insert(0, normalizedSection + INTEGER_UNITS[0][i]);
            }
        }

        return prefix + normalizeChineseAmount(integerBuilder + result.toString());
    }

    /**
     * 将金额转换为中文大写。
     *
     * @param amount 金额
     * @return 中文大写金额
     */
    public static String toChineseUppercase(double amount) {
        return toChineseUppercase(BigDecimal.valueOf(amount));
    }

    /**
     * 将金额字符串解析为金额对象。
     *
     * @param amountText 金额字符串
     * @return 金额对象
     */
    public static BigDecimal parse(String amountText) {
        if (amountText == null || amountText.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(amountText.trim());
    }

    /**
     * 将金额字符串解析为分。
     *
     * @param amountText 金额字符串
     * @return 分
     */
    public static long parseToCent(String amountText) {
        return toCent(parse(amountText));
    }

    /**
     * 将数字金额转换为分。
     *
     * @param amount 金额
     * @return 分
     */
    public static long toCent(Number amount) {
        if (amount == null) {
            return 0L;
        }
        if (amount instanceof BigDecimal bigDecimal) {
            return toCent(bigDecimal);
        }
        if (amount instanceof BigInteger bigInteger) {
            return toCent(new BigDecimal(bigInteger));
        }
        return toCent(new BigDecimal(amount.toString()));
    }

    /**
     * 规范化中文金额整数部分。
     *
     * @param text 原文本
     * @return 规范化后的文本
     */
    private static String normalizeChineseInteger(String text) {
        return text.replaceAll("零+", "零").replaceAll("零$", "");
    }

    /**
     * 规范化中文金额结果。
     *
     * @param text 原文本
     * @return 规范化后的文本
     */
    private static String normalizeChineseAmount(String text) {
        return text.replaceAll("零+", "零")
                .replaceAll("零万", "万")
                .replaceAll("零亿", "亿")
                .replaceAll("亿万", "亿")
                .replaceAll("零元", "元")
                .replaceAll("零角零分$", "整")
                .replaceAll("零分$", "")
                .replaceAll("元整$", "元整")
                .replaceAll("^元", "零元");
    }
}
