package com.whim.system.model.vo.config;

import com.whim.system.model.config.ConfigOption;
import com.whim.system.model.entity.SysConfig;
import com.whim.system.model.enums.ConfigValueType;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 系统与自定义配置的列表数据。
 */
@Data
@AutoMapper(target = SysConfig.class, convertGenerate = false)
public class SysConfigVO {
    private Long id;
    private String configName;
    private String configKey;
    private ConfigValueType valueType;
    private List<ConfigOption> valueOptions;
    private String configValue;
    private String remark;
    private Boolean builtIn;
}
