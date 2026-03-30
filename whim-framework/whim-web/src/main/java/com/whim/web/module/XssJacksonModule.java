package com.whim.web.module;

import com.whim.web.deserializer.XssDeserializerModifier;
import tools.jackson.databind.module.SimpleModule;

/**
 * @author Jince
 * @date 2026/03/28
 * @description XSS Jackson 模块，负责注册基于注解的字符串清洗能力。
 */
public final class XssJacksonModule extends SimpleModule {

    /**
     * 创建 XSS Jackson 模块。
     */
    public XssJacksonModule() {
        super("whim-xss-jackson-module");
        setDeserializerModifier(new XssDeserializerModifier());
    }
}
