package com.whim.json.handler;

import com.whim.json.serializer.DesensitizeSerializerModifier;
import com.whim.json.spi.DesensitizationAccessEvaluator;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.module.SimpleModule;

import java.util.Objects;

/**
 * @author Jince
 * @date 2026/03/27
 * @description 字段脱敏 Jackson 处理器。
 */
public final class DesensitizeJacksonHandler {
    private final DesensitizationAccessEvaluator accessEvaluator;

    /**
     * 创建字段脱敏 Jackson 处理器。
     *
     * @param accessEvaluator 脱敏访问评估器
     */
    public DesensitizeJacksonHandler(DesensitizationAccessEvaluator accessEvaluator) {
        this.accessEvaluator = Objects.requireNonNull(accessEvaluator, "accessEvaluator must not be null");
    }

    /**
     * 构建字段脱敏处理模块。
     *
     * @return 字段脱敏处理模块
     */
    public JacksonModule getModule() {
        return new SimpleModule("whim-desensitize-jackson-module")
                .setSerializerModifier(new DesensitizeSerializerModifier(accessEvaluator));
    }
}
