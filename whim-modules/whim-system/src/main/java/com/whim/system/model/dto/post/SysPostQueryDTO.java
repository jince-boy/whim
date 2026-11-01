package com.whim.system.model.dto.post;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 岗位分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPostQueryDTO extends PageQueryDTO {
    /** 岗位名称，模糊匹配。 */
    @Size(max = 64, message = "岗位名称不能超过64个字符")
    private String postName;

    /** 岗位编码，模糊匹配。 */
    @Size(max = 64, message = "岗位编码不能超过64个字符")
    private String postCode;

    /** 所属部门ID，精确匹配。 */
    @Positive(message = "所属部门ID必须大于0")
    private Long deptId;

    /** 仅查询全组织岗位，不能同时指定部门。 */
    private Boolean organizationWide = false;

    /** 状态：0正常、1停用。 */
    @Min(value = 0, message = "岗位状态无效")
    @Max(value = 1, message = "岗位状态无效")
    private Integer status;
}
