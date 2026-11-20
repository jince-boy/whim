package com.whim.system.model.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.jdk.StringDeserializer;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 单选配置的选项名称和实际存储值。
 */
@Data
public class ConfigOption {
    @NotBlank(message = "选项名称不能为空")
    @Size(max = 128, message = "选项名称不能超过128个字符")
    private String label;

    @NotBlank(message = "选项值不能为空")
    @Size(max = 128, message = "选项值不能超过128个字符")
    @JsonDeserialize(using = StringDeserializer.class)
    private String value;
}
