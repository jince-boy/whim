package com.whim.system.model.dto.post;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/03
 * @description 岗位快捷启停参数。
 */
@Data
public class SysPostStatusDTO {
    /** 岗位ID。 */
    @NotNull(message = "岗位ID不能为空")
    private Long id;
    /** 状态：0正常、1停用。 */
    @NotNull(message = "岗位状态不能为空")
    @Min(value = 0, message = "岗位状态无效")
    @Max(value = 1, message = "岗位状态无效")
    private Integer status;
}
