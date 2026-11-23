package com.whim.system.model.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.whim.mybatisplus.model.entity.BaseEntity;
import com.whim.system.model.enums.ConfigValueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.jdk.StringDeserializer;

import java.io.Serial;
import java.util.List;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 应用业务配置。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(autoResultMap = true)
public class SysConfig extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String configName;

    @TableField(updateStrategy = FieldStrategy.NEVER)
    private String configKey;
    private ConfigValueType valueType;
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<ConfigOption> valueOptions;
    private String configValue;
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String remark;
    @TableField(updateStrategy = FieldStrategy.NEVER)
    private Boolean builtIn;
    @TableLogic
    private Long deleted;

    /**
     * @author Jince
     * @date 2026/11/20
     * @description 单选配置的选项名称和实际存储值。
     */
    @Data
    public static class ConfigOption {
        @NotBlank(message = "选项名称不能为空")
        @Size(max = 128, message = "选项名称不能超过128个字符")
        private String label;

        @NotBlank(message = "选项值不能为空")
        @Size(max = 128, message = "选项值不能超过128个字符")
        @JsonDeserialize(using = StringDeserializer.class)
        private String value;
    }
}
