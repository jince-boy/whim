package com.whim.system.model.dto.homepage;

import com.whim.system.model.entity.SysHomePage;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 新增首页配置。
 */
@Data
@AutoMapper(target = SysHomePage.class, reverseConvertGenerate = false)
public class SysHomePageAddDTO {
    @NotNull(message = "请选择关联类型")
    @Min(value = 1, message = "关联类型无效")
    @Max(value = 4, message = "关联类型无效")
    private Integer targetType;
    private Long targetId;
    @NotBlank(message = "请输入首页路由")
    @Size(max = 255, message = "首页路由不能超过255个字符")
    @Pattern(regexp = "(/[A-Za-z][A-Za-z0-9]*)+", message = "首页路由必须是由字母和数字组成的站内静态路径")
    private String path;
    @NotBlank(message = "请输入页面组件路径")
    @Size(max = 255, message = "组件路径不能超过255个字符")
    private String component;
    @NotNull(message = "请输入优先级")
    @PositiveOrZero(message = "优先级不能为负数")
    private Integer priority;
    @NotNull(message = "请选择启用状态")
    @Min(value = 0, message = "启用状态无效")
    @Max(value = 1, message = "启用状态无效")
    private Integer status;
}
