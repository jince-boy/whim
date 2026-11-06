package com.whim.system.model.dto.post;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/03
 * @description 岗位分页查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPostQueryDTO extends PageQueryDTO {
    /** 岗位名称，包含匹配。 */
    @Size(max = 64, message = "岗位名称不能超过64个字符")
    private String postName;
    /** 岗位编码，包含匹配。 */
    @Size(max = 64, message = "岗位编码不能超过64个字符")
    private String postCode;
    /** 所选部门ID，查询本部门及全部下级部门的岗位；省略时不限制部门。 */
    private Long deptId;
    /** true时仅查询全组织岗位，false或省略时不限制。 */
    private Boolean organizationWide;
    /** 状态：0正常、1停用。 */
    @Min(value = 0, message = "岗位状态无效")
    @Max(value = 1, message = "岗位状态无效")
    private Integer status;

    /**
     * 校验全组织筛选与具体部门筛选互斥。
     *
     * @return 是否仅指定一种部门筛选条件
     */
    @AssertTrue(message = "查询全组织岗位时不能同时指定所属部门")
    public boolean isDepartmentFilterValid() {
        return !Boolean.TRUE.equals(organizationWide) || deptId == null;
    }
}
