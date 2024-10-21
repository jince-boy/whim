package com.whim.common.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Jince
 * date: 2024/10/3 23:25
 * description: 控制器基类
 */
@Slf4j
public class BaseController {
    /**
     * 时间格式化器数组，用于解析不同格式的日期字符串
     */
    private static final DateTimeFormatter[] dateTimeFormatters = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy.MM")
    };

    /**
     * 将字符串解析为 LocalDateTime 对象
     *
     * @param str 需要解析的日期字符串
     * @return 返回解析后的 LocalDateTime，若解析失败则返回 Optional.empty()
     */
    public Optional<LocalDateTime> parseLocalDateTime(Object str) {
        if (Objects.isNull(str)) {
            return Optional.empty();
        }
        for (DateTimeFormatter formatter : dateTimeFormatters) {
            try {
                return Optional.of(LocalDateTime.parse(str.toString(), formatter));
            } catch (DateTimeParseException ignored) {
                // 忽略当前格式的解析异常，继续尝试下一个格式
            }
        }
        // 如果所有格式都无法解析，记录错误信息
        log.error("无法解析日期: {}", str);
        return Optional.empty();
    }

    /**
     * 初始化数据绑定器，注册自定义编辑器以支持 LocalDateTime 类型的转换
     *
     * @param binder WebDataBinder 对象
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDateTime.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                // 使用 parseLocalDateTime 方法进行转换，并设置值
                setValue(parseLocalDateTime(text).orElse(null));
            }
        });
    }
}
