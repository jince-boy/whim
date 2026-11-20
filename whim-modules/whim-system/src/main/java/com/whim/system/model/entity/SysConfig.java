package com.whim.system.model.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import com.whim.mybatisplus.model.entity.BaseEntity;
import com.whim.system.model.config.ConfigOption;
import com.whim.system.model.enums.ConfigValueType;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
}
