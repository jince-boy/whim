package com.whim.json.config;

import com.whim.json.config.properties.DateTimeProperties;
import com.whim.json.module.BigNumberJacksonModule;
import com.whim.json.module.DateTimeJacksonModule;
import com.whim.json.utils.JsonUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

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
     * 创建时间 Jackson 模块。
     *
     * @param dateTimeProperties 时间配置属性
     * @return 时间 Jackson 模块
     */
    @Bean
    public DateTimeJacksonModule dateTimeJacksonModule(DateTimeProperties dateTimeProperties) {
        return new DateTimeJacksonModule(dateTimeProperties);
    }

    /**
     * 创建大数字 Jackson 模块。
     *
     * @return 大数字 Jackson 模块
     */
    @Bean
    public BigNumberJacksonModule bigNumberJacksonModule() {
        return new BigNumberJacksonModule();
    }

    /**
     * 将序列化基础模块注册到 Spring Boot 管理的 JsonMapper。
     *
     * @param dateTimeJacksonModule  时间 Jackson 模块
     * @param bigNumberJacksonModule 大数字 Jackson 模块
     * @param dateTimeProperties     时间配置属性
     * @return JsonMapper 构建器自定义器
     */
    @Bean
    public JsonMapperBuilderCustomizer serializationJsonMapperCustomizer(
            DateTimeJacksonModule dateTimeJacksonModule,
            BigNumberJacksonModule bigNumberJacksonModule,
            DateTimeProperties dateTimeProperties) {
        return builder -> builder
                .addModules(dateTimeJacksonModule, bigNumberJacksonModule)
                .defaultTimeZone(TimeZone.getTimeZone(dateTimeProperties.getZoneId()));
    }

    /**
     * 初始化 JsonUtils 使用的 JsonMapper
     *
     * @param jsonMapper Spring Boot 管理的 JsonMapper
     * @return JsonUtils 初始化器
     */
    @Bean
    public JsonUtils.Initializer jsonUtilsInitializer(JsonMapper jsonMapper) {
        return new JsonUtils.Initializer(jsonMapper);
    }
}
