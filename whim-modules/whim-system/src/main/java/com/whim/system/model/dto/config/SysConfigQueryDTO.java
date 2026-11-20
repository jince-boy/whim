package com.whim.system.model.dto.config;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 配置分页查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysConfigQueryDTO extends PageQueryDTO {
    @Size(max = 128, message = "配置名称不能超过128个字符")
    private String configName;

    @NotNull(message = "请选择配置分类")
    private Boolean builtIn;

    @Size(max = 128, message = "配置键不能超过128个字符")
    private String configKey;

    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
