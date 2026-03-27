package com.whim.json.config;

import com.whim.json.handler.BigNumberJacksonHandler;
import com.whim.json.handler.DesensitizeJacksonHandler;
import com.whim.json.handler.TimeJacksonHandler;
import com.whim.json.spi.DefaultDesensitizationAccessEvaluator;
import com.whim.json.spi.DesensitizationAccessEvaluator;
import com.whim.json.utils.JsonUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

import java.util.TimeZone;

/**
 * @author Jince
 * @date 2026/03/27
 * @description Jackson 自动配置
 */
@AutoConfiguration(before = JacksonAutoConfiguration.class)
public class JacksonConfig {

    /**
     * 创建时间处理器
     *
     * @return 时间处理器
     */
    @Bean
    public TimeJacksonHandler timeJacksonHandler() {
        return new TimeJacksonHandler();
    }

    /**
     * 创建大数字处理器
     *
     * @return 大数字处理器
     */
    @Bean
    public BigNumberJacksonHandler bigNumberJacksonHandler() {
        return new BigNumberJacksonHandler();
    }

    /**
     * 创建默认脱敏访问评估器
     *
     * @return 默认脱敏访问评估器
     */
    @Bean
    @ConditionalOnMissingBean(DesensitizationAccessEvaluator.class)
    public DesensitizationAccessEvaluator desensitizationAccessEvaluator() {
        return new DefaultDesensitizationAccessEvaluator();
    }

    /**
     * 创建字段脱敏处理器
     *
     * @param accessEvaluator 脱敏访问评估器
     * @return 字段脱敏处理器
     */
    @Bean
    public DesensitizeJacksonHandler desensitizeJacksonHandler(DesensitizationAccessEvaluator accessEvaluator) {
        return new DesensitizeJacksonHandler(accessEvaluator);
    }

    /**
     * 配置 JsonMapper
     *
     * @param timeJacksonHandler      时间处理器
     * @param bigNumberJacksonHandler 大数字处理器
     * @param desensitizeJacksonHandler 字段脱敏处理器
     * @return JsonMapper 构建器自定义器
     */
    @Bean
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer(
            TimeJacksonHandler timeJacksonHandler,
            BigNumberJacksonHandler bigNumberJacksonHandler,
            DesensitizeJacksonHandler desensitizeJacksonHandler
    ) {
        return builder -> builder
                .addModules(
                        timeJacksonHandler.getModule(),
                        bigNumberJacksonHandler.getModule(),
                        desensitizeJacksonHandler.getModule()
                )
                .defaultTimeZone(TimeZone.getDefault());
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
