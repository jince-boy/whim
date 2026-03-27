package com.whim.json.module;

import com.whim.json.serializer.DesensitizeSerializerModifier;
import com.whim.json.spi.DesensitizationAccessEvaluator;
import tools.jackson.databind.module.SimpleModule;

import java.util.Objects;

/**
 * @author Jince
 * @date 2026/03/28
 * @description 脱敏 Jackson 模块，负责注册基于注解的字段脱敏能力。
 */
public final class DesensitizeJacksonModule extends SimpleModule {

    /**
     * 创建脱敏 Jackson 模块。
     *
     * @param accessEvaluator 脱敏访问评估器
     */
    public DesensitizeJacksonModule(DesensitizationAccessEvaluator accessEvaluator) {
        super("whim-desensitize-jackson-module");
        setSerializerModifier(new DesensitizeSerializerModifier(
                Objects.requireNonNull(accessEvaluator, "accessEvaluator must not be null")
        ));
    }
}
