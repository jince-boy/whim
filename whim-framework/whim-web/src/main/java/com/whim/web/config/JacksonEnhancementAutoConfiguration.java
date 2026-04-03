package com.whim.web.config;

import com.whim.web.deserializer.XssDeserializerModifier;
import com.whim.web.serializer.DesensitizeSerializerModifier;
import com.whim.web.spi.DesensitizationAccessEvaluator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.module.SimpleModule;

/**
 * @author Jince
 * @date 2026/03/30
 * @description Jackson 增强自动配置，注册面向 Web 场景的脱敏、XSS 防护模块。
 */
@AutoConfiguration(before = JacksonAutoConfiguration.class)
public class JacksonEnhancementAutoConfiguration {

    /**
     * 创建默认脱敏访问评估器
     *
     * @return 默认脱敏访问评估器
     */
    @Bean
    @ConditionalOnMissingBean(DesensitizationAccessEvaluator.class)
    public DesensitizationAccessEvaluator desensitizationAccessEvaluator() {
        return (roles, authorities, requireAll) -> false;
    }

    /**
     * 将 Web 增强模块注册到 Spring Boot 管理的 JsonMapper。
     *
     * @param accessEvaluator 脱敏访问评估器
     * @return JsonMapper 构建器自定义器
     */
    @Bean
    public JsonMapperBuilderCustomizer webJsonEnhancementCustomizer(DesensitizationAccessEvaluator accessEvaluator) {
        return builder -> builder.addModules(
                createDesensitizeModule(accessEvaluator),
                createXssModule()
        );
    }

    /**
     * 创建脱敏 Jackson 模块。
     *
     * @param accessEvaluator 脱敏访问评估器
     * @return 脱敏 Jackson 模块
     */
    private SimpleModule createDesensitizeModule(DesensitizationAccessEvaluator accessEvaluator) {
        var module = new SimpleModule("whim-desensitize-jackson-module");
        module.setSerializerModifier(new DesensitizeSerializerModifier(accessEvaluator));
        return module;
    }

    /**
     * 创建 XSS Jackson 模块。
     *
     * @return XSS Jackson 模块
     */
    private SimpleModule createXssModule() {
        var module = new SimpleModule("whim-xss-jackson-module");
        module.setDeserializerModifier(new XssDeserializerModifier());
        return module;
    }
}
