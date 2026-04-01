package com.whim.json.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;

/**
 * @author Jince
 * @date 2026/03/28
 * @description 时间格式配置属性。
 */
@Data
@ConfigurationProperties(prefix = "whim.time")
public class DateTimeProperties {
    /**
     * 统一序列化与本地时间解析使用的时区。
     */
    private String zoneId = ZoneId.systemDefault().getId();

    /**
     * 日期时间统一输出格式。
     */
    private String dateTimePattern = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期统一输出格式。
     */
    private String datePattern = "yyyy-MM-dd";

    /**
     * 时间统一输出格式。
     */
    private String timePattern = "HH:mm:ss";

}
