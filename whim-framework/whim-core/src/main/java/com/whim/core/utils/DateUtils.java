package com.whim.core.utils;

import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类，提供常用的日期处理方法。
 */
public class DateUtils {

    public static final String FORMAT_01 = "yyyy-MM-dd";
    public static final String FORMAT_02 = "HH:mm:ss";
    public static final String FORMAT_03 = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_04 = "yyyyMMddHHmmss";
    public static final String FORMAT_05 = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String FORMAT_06 = "yyyyMMdd";
    public static final String FORMAT_07 = "HHmmss";
    public static final String FORMAT_08 = "yyyy/MM/dd";
    public static final String FORMAT_09 = "yyyyMMdd HH:mm:ss";
    public static final String FORMAT_10 = "yyyyMM";
    public static final String FORMAT_11 = "yyyy";

    /**
     * 获取当前日期时间。
     *
     * @return 当前日期时间的 Date 对象。
     */
    public static Date now() {
        return Date.from(Instant.now());
    }

    /**
     * 将字符串解析为日期。
     *
     * @param date    日期字符串。
     * @param pattern 日期格式模式。
     * @return 解析后的 Date 对象。
     * @throws IllegalArgumentException 如果字符串无法按指定模式解析。
     */
    public static Date parse(String date, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setLenient(false);
            return sdf.parse(date);
        } catch (Exception e) {
            throw new IllegalArgumentException(date + "无法按模式解析: " + pattern, e);
        }
    }
    /**
     * 自动识别常见日期格式并解析为Date对象
     *
     * @param dateStr 日期字符串
     * @return 解析后的Date对象
     * @throws IllegalArgumentException 如果无法识别格式或解析失败
     */
    public static Date parse(String dateStr) {
        if (StringUtils.isBlank(dateStr)) {
            throw new IllegalArgumentException("日期字符串不能为空");
        }

        // 定义常见日期格式模式（按优先级排序）
        String[] patterns = {
                FORMAT_03,  // yyyy-MM-dd HH:mm:ss
                FORMAT_01,  // yyyy-MM-dd
                FORMAT_05,  // yyyy-MM-dd HH:mm:ss.SSS
                FORMAT_08,  // yyyy/MM/dd
                FORMAT_04,  // yyyyMMddHHmmss
                FORMAT_06,  // yyyyMMdd
                FORMAT_09,  // yyyyMMdd HH:mm:ss
                FORMAT_10,  // yyyyMM
                FORMAT_11,  // yyyy
                FORMAT_02   // HH:mm:ss
        };

        // 尝试按格式优先级解析
        for (String pattern : patterns) {
            try {
                // 长度匹配检查（提高效率）
                if (dateStr.length() == pattern.length()) {
                    return parse(dateStr, pattern);
                }
            } catch (IllegalArgumentException e) {
                // 忽略当前格式的解析错误，继续尝试下一个格式
                continue;
            }
        }

        throw new IllegalArgumentException("无法识别的日期格式: " + dateStr);
    }
    /**
     * 将日期格式化为字符串。
     *
     * @param date    要格式化的日期对象。
     * @param pattern 日期格式模式。
     * @return 格式化后的日期字符串，如果日期为 null，则返回 null。
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(false);
        return sdf.format(date);
    }

    /**
     * 转换日期字符串的格式。
     *
     * @param timeStr      要转换的日期字符串。
     * @param sourceFormat 源格式模式。
     * @param targetFormat 目标格式模式。
     * @return 转换后的日期字符串，如果转换失败则返回 null。
     */
    public static String convertFormat(String timeStr, String sourceFormat, String targetFormat) {
        try {
            SimpleDateFormat sourceSdf = new SimpleDateFormat(sourceFormat);
            SimpleDateFormat targetSdf = new SimpleDateFormat(targetFormat);
            Date date = sourceSdf.parse(timeStr);
            return targetSdf.format(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取指定日期前后几天的日期字符串。
     *
     * @param dateString 要操作的日期字符串。
     * @param days       天数，可以为负值表示之前的日期。
     * @return 计算后的日期字符串，格式为 yyyyMMddHHmmss。
     * @throws ParseException 如果输入的日期字符串无法解析。
     */
    public static String getOffsetDate(String dateString, int days) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_04);
        Calendar calendar = Calendar.getInstance();
        if (StringUtils.isNotBlank(dateString)) {
            Date date = sdf.parse(dateString);
            calendar.setTime(date);
        }
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return sdf.format(calendar.getTime());
    }

    /**
     * 获取当前时间戳。
     *
     * @return 当前时间戳的 Timestamp 对象。
     */
    public static Timestamp getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * 获取当前日期字符串，使用指定格式进行格式化。
     *
     * @param pattern 日期格式模式。
     * @return 当前日期的字符串表示形式。
     */
    public static String formatCurrentDate(String pattern) {
        return format(now(), pattern);
    }

    /**
     * 获取当前日期时间字符串，使用指定格式进行格式化。
     *
     * @param pattern 日期时间格式模式。
     * @return 当前日期时间的字符串表示形式。
     */
    public static String formatCurrentDateTime(String pattern) {
        return format(now(), pattern);
    }

    /**
     * 获取当前日期（不包含时间部分）。
     *
     * @return 当前日期的 Date 对象，时间部分已清零。
     */
    public static Date currentDate() {
        return truncateTime(now());
    }

    /**
     * 将给定日期的时间部分清零（设置为午夜）。
     *
     * @param date 要处理的 Date 对象。
     * @return 清零后的 Date 对象（仅包含日期部分）。
     */
    public static Date truncateTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * 获取当前年月日字符串，格式为 yyyyMMdd。
     *
     * @return 当前年月日的字符串表示形式。
     */
    public static String getCurrentDate() {
        return formatCurrentDate(FORMAT_06);
    }

    /**
     * 获取当前年月字符串，格式为 yyyyMM。
     *
     * @return 当前年月的字符串表示形式。
     */
    public static String getCurrentMonth() {
        return formatCurrentDate(FORMAT_10);
    }

    /**
     * 获取当前年份字符串，格式为 yyyy。
     *
     * @return 当前年份的字符串表示形式。
     */
    public static String getCurrentYear() {
        return formatCurrentDate(FORMAT_11);
    }

    /**
     * 获取当前时分秒字符串，格式为 HHmmss。
     *
     * @return 当前时分秒的字符串表示形式。
     */
    public static String getCurrentTime() {
        return format(now(), FORMAT_07);
    }

    /**
     * 校验给定的日期字符串是否符合指定格式。
     *
     * @param dateStr 日期字符串要校验的内容
     * @param format  日期格式模式
     * @return 如果符合指定格式则返回 true，否则返回 false
     */
    public static boolean isValidDate(String dateStr, String format) {
        if (StringUtils.isBlank(dateStr)) {
            return false;
        }

        if (dateStr.length() != format.length()) {
            return false;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(format);

        try {
            dateFormat.setLenient(false);
            dateFormat.parse(dateStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取前n天的日期字符串，使用默认格式 yyyyMMdd
     *
     * @param days 前n天数
     * @return 前n天的日期字符串
     */
    public static String getDateBefore(int days) {
        return getDateBeforeWithFormat(FORMAT_06, days);
    }

    /**
     * 获取前n天的日期字符串，使用指定格式
     *
     * @param format 指定输出的日期格式
     * @param days   前n天数
     * @return 前n天的日期字符串
     */
    public static String getDateBeforeWithFormat(String format, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -days); // 将当前时间减去days天
        SimpleDateFormat formatter = new SimpleDateFormat(format); // 创建SimpleDateFormat对象
        return formatter.format(calendar.getTime()); // 返回减去days后的时间
    }

    /**
     * 获取前n天的日期字符串，基于指定日期字符串
     *
     * @param dateStr 指定的基准日期字符串
     * @param days    前n天数
     * @return 基于指定基准日期减去days后的日期字符串
     */
    public static String getDateBefore(String dateStr, int days) {
        Date date = parse(dateStr, FORMAT_06); // 将输入的dateStr解析成一个date对象
        Calendar calendar = Calendar.getInstance(); // 创建一个Calendar对象
        calendar.setTime(date); // 将calendar设置成date对象
        calendar.add(Calendar.DATE, -days); // 将calendar减去days天
        SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_06); // 创建SimpleDateFormat对象
        return formatter.format(calendar.getTime()); // 返回减去days后的时间
    }

    /**
     * 计算两个日期之间的天数差
     *
     * @param date1 第一个日期对象
     * @param date2 第二个日期对象
     * @return 两个日期之间相差的天数（绝对值）
     */
    public static int daysBetween(Date date1, Date date2) {
        long diffInMillis = Math.abs(date2.getTime() - date1.getTime());
        return (int) (diffInMillis / (1000 * 60 * 60 * 24));
    }

    /**
     * 将 LocalDateTime 转换为 Date 对象
     *
     * @param localDateTime 要转换的 LocalDateTime 对象
     * @return 转换后的 Date 对象
     */
    public static Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 将 Date 对象转换为 LocalDateTime 对象
     *
     * @param date 要转换的 Date 对象
     * @return 转换后的 LocalDateTime 对象
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}