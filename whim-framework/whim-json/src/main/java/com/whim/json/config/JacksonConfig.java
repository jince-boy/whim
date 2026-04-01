package com.whim.json.config;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.whim.json.config.properties.DateTimeProperties;
import com.whim.json.module.BigNumberJacksonModule;
import com.whim.json.utils.JsonUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Jince
 * @date 2026/03/27
 * @description Jackson 自动配置，注册时间、大数字等通用序列化模块。
 */
@AutoConfiguration(before = JacksonAutoConfiguration.class)
@EnableConfigurationProperties(DateTimeProperties.class)
public class JacksonConfig {
    /**
     * 配置 Jackson 全局日期时间格式与通用模块。
     */
    @Bean
    public JsonMapperBuilderCustomizer serializationJsonMapperCustomizer(
            DateTimeProperties props) {
        var zone = TimeZone.getTimeZone(props.getZoneId());
        var dtFormat = JsonFormat.Value.forPattern(props.getDateTimePattern()).withTimeZone(zone);
        return builder -> builder
                .addModules(new BigNumberJacksonModule())
                .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
                .defaultTimeZone(zone)
                .withConfigOverride(LocalDateTime.class, o -> o.setFormat(JsonFormat.Value.forPattern(props.getDateTimePattern())))
                .withConfigOverride(LocalDate.class, o -> o.setFormat(JsonFormat.Value.forPattern(props.getDatePattern())))
                .withConfigOverride(LocalTime.class, o -> o.setFormat(JsonFormat.Value.forPattern(props.getTimePattern())))
                .withConfigOverride(Instant.class, o -> o.setFormat(dtFormat))
                .withConfigOverride(Date.class, o -> o.setFormat(dtFormat))
                .withConfigOverride(OffsetDateTime.class, o -> o.setFormat(dtFormat))
                .withConfigOverride(ZonedDateTime.class, o -> o.setFormat(dtFormat));
    }

    /**
     * 在 Spring 启动时，把 Spring 管理的 JsonMapper 塞进 JsonUtils 这个静态工具类里
     *
     * @param jsonMapper Spring 管理的 JsonMapper
     * @return JsonUtils.Initializer
     */
    @Bean
    public JsonUtils.Initializer jsonUtilsInitializer(JsonMapper jsonMapper) {
        return new JsonUtils.Initializer(jsonMapper);
    }
}
