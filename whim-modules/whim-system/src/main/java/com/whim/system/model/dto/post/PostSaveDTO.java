package com.whim.system.model.dto.post;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 当前租户岗位保存参数。
 */
@Data
public class PostSaveDTO {
    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 64, message = "岗位名称不能超过64个字符")
    private String postName;

    @NotBlank(message = "岗位编码不能为空")
    @Size(max = 64, message = "岗位编码不能超过64个字符")
    private String postCode;

    private Long deptId;

    @NotNull(message = "排序值不能为空")
    @Min(value = 0, message = "排序值不能为负数")
    private Integer sort;

    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
