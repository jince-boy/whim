package com.whim.web.config;

import com.whim.web.module.DesensitizeJacksonModule;
import com.whim.web.module.XssJacksonModule;
import com.whim.web.spi.DefaultDesensitizationAccessEvaluator;
import com.whim.web.spi.DesensitizationAccessEvaluator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

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
     * 将 Web 增强模块注册到 Spring Boot 管理的 JsonMapper。
     *
     * @param desensitizeJacksonModule 脱敏 Jackson 模块
     * @param xssJacksonModule         XSS Jackson 模块
     * @return JsonMapper 构建器自定义器
     */
    @Bean
    public JsonMapperBuilderCustomizer webJsonEnhancementCustomizer(
            DesensitizeJacksonModule desensitizeJacksonModule,
            XssJacksonModule xssJacksonModule) {
        return builder -> builder.addModules(desensitizeJacksonModule, xssJacksonModule);
    }
}
