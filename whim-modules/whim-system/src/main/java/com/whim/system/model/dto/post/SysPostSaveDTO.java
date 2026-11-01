package com.whim.system.model.dto.post;

import com.whim.system.model.entity.SysPost;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 岗位新增参数及修改时复用的业务字段，不包含角色或功能授权。
 */
@Data
@AutoMapper(target = SysPost.class, reverseConvertGenerate = false)
public class SysPostSaveDTO {
    /** 所属部门ID，null表示全组织岗位。 */
    @Positive(message = "所属部门ID必须大于0")
    private Long deptId;

    /** 岗位名称。 */
    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 64, message = "岗位名称不能超过64个字符")
    private String postName;

    /** 岗位编码，在未删除的岗位中唯一。 */
    @NotBlank(message = "岗位编码不能为空")
    @Size(max = 64, message = "岗位编码不能超过64个字符")
    private String postCode;

    /** 排序值。 */
    @NotNull(message = "排序值不能为空")
    @PositiveOrZero(message = "排序值不能小于0")
    private Integer sort = 0;

    /** 状态：0正常、1停用。 */
    @NotNull(message = "岗位状态不能为空")
    @Min(value = 0, message = "岗位状态无效")
    @Max(value = 1, message = "岗位状态无效")
    private Integer status = 0;

    /** 备注，可清空。 */
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
