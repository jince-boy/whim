package com.whim.system.model.dto.homepage;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 按类型和关键词搜索首页关联对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HomeTargetQueryDTO extends PageQueryDTO {
    @NotNull(message = "请选择关联类型")
    @Min(value = 1, message = "关联类型无效")
    @Max(value = 3, message = "关联类型无效")
    private Integer targetType;
    @Size(max = 64, message = "关键词不能超过64个字符")
    private String keyword;
}
