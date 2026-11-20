package com.whim.system.model.dto.config;

import com.whim.system.model.config.ConfigOption;
import com.whim.system.model.entity.SysConfig;
import com.whim.system.model.enums.ConfigValueType;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.jdk.StringDeserializer;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 修改配置参数。
 */
@Data
@AutoMapper(target = SysConfig.class, reverseConvertGenerate = false)
public class SysConfigUpdateDTO {
    @Size(max = 128, message = "配置名称不能超过128个字符")
    private String configName;

    @NotNull(message = "配置ID不能为空")
    private Long id;

    private ConfigValueType valueType;

    @Size(max = 100, message = "单选选项不能超过100项")
    private List<@NotNull(message = "选项不能为空") @Valid ConfigOption> valueOptions;

    /** 配置值可为空字符串，保留业务文本的首尾空白。 */
    @NotNull(message = "请提供配置值")
    @JsonDeserialize(using = StringDeserializer.class)
    private String configValue;

    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
