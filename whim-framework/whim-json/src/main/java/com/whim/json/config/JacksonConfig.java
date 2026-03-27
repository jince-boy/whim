package com.whim.json.config;

import com.whim.json.module.BigNumberJacksonModule;
import com.whim.json.module.DesensitizeJacksonModule;
import com.whim.json.module.TimeJacksonModule;
import com.whim.json.module.XssJacksonModule;
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
     * 创建时间 Jackson 模块。
     *
     * @return 时间 Jackson 模块
     */
    @Bean
    public TimeJacksonModule timeJacksonModule() {
        return new TimeJacksonModule();
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
     * 创建脱敏 Jackson 模块。
     *
     * @param accessEvaluator 脱敏访问评估器
     * @return 脱敏 Jackson 模块
     */
    @Bean
    public DesensitizeJacksonModule desensitizeJacksonModule(DesensitizationAccessEvaluator accessEvaluator) {
        return new DesensitizeJacksonModule(accessEvaluator);
    }

    /**
     * 创建 XSS Jackson 模块。
     *
     * @return XSS Jackson 模块
     */
    @Bean
    public XssJacksonModule xssJacksonModule() {
        return new XssJacksonModule();
    }

    /**
     * 配置 JsonMapper。
     *
     * @param timeJacksonModule 时间 Jackson 模块
     * @param bigNumberJacksonModule 大数字 Jackson 模块
     * @param desensitizeJacksonModule 脱敏 Jackson 模块
     * @param xssJacksonModule XSS Jackson 模块
     * @return JsonMapper 构建器自定义器
     */
    @Bean
    public JsonMapperBuilderCustomizer jsonMapperBuilderCustomizer(
            TimeJacksonModule timeJacksonModule,
            BigNumberJacksonModule bigNumberJacksonModule,
            DesensitizeJacksonModule desensitizeJacksonModule,
            XssJacksonModule xssJacksonModule
    ) {
        return builder -> builder
                .addModules(
                        timeJacksonModule,
                        bigNumberJacksonModule,
                        desensitizeJacksonModule,
                        xssJacksonModule
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
