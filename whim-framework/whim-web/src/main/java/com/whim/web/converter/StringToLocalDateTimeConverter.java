package com.whim.web.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author jince
 * date: 2025/8/20 13:43
 * description: LocalDateTime转换器
 */
public class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
    private static final String[] PATTERNS = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm"
    };

    @Override
    public LocalDateTime convert(@Nullable String source) {
        // 1. 处理空值
        if (source == null || source.trim().isEmpty()) {
            return null; // 或者返回默认值，如 LocalDateTime.now()
        }

        // 2. 尝试多种格式解析
        for (String pattern : PATTERNS) {
            try {
                return LocalDateTime.parse(source.trim(), DateTimeFormatter.ofPattern(pattern));
            } catch (Exception ignored) {
                // 继续尝试下一个格式
            }
        }

        // 3. 所有格式均不匹配时抛出异常（或返回 null，根据业务需求）
        throw new IllegalArgumentException("无效的日期格式: " + source);
    }
}
