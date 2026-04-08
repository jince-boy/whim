package com.whim.web.config;

import com.whim.web.deserializer.XssDeserializerModifier;
import com.whim.web.xss.DefaultXssSanitizer;
import com.whim.web.xss.XssSanitizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.module.SimpleModule;

/**
 * Registers JSON-side XSS sanitizing while keeping the sanitizer itself transport-agnostic.
 */
@AutoConfiguration(before = JacksonAutoConfiguration.class)
public class JacksonEnhancementAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public XssSanitizer xssSanitizer() {
        return new DefaultXssSanitizer();
    }

    @Bean
    public JsonMapperBuilderCustomizer webJsonEnhancementCustomizer(XssSanitizer xssSanitizer) {
        return builder -> builder.addModules(createXssModule(xssSanitizer));
    }

    private SimpleModule createXssModule(XssSanitizer xssSanitizer) {
        var module = new SimpleModule("whim-xss-jackson-module");
        module.setDeserializerModifier(new XssDeserializerModifier(xssSanitizer));
        return module;
    }
}
