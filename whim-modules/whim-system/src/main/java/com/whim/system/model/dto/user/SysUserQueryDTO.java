package com.whim.system.model.dto.user;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/06
 * @description 用户分页查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserQueryDTO extends PageQueryDTO {
    /** 用户名，包含匹配。 */
    @Size(max = 32, message = "用户名不能超过32个字符")
    private String username;
    /** 姓名，包含匹配。 */
    @Size(max = 24, message = "姓名不能超过24个字符")
    private String name;
    /** 手机号码，包含匹配。 */
    @Size(max = 20, message = "手机号码不能超过20个字符")
    private String phone;
    /** 查询本部门及全部下级部门的用户。 */
    private Long deptId;
    /** true时仅查询未分配部门的用户。 */
    private Boolean unassigned;
    /** 用户状态：0启用、1停用。 */
    @Min(value = 0, message = "用户状态无效")
    @Max(value = 1, message = "用户状态无效")
    private Integer status;

    /**
     * 校验未分配部门筛选与具体部门筛选互斥。
     *
     * @return 是否只指定一种部门筛选条件
     */
    @AssertTrue(message = "查询未分配部门用户时不能同时指定部门")
    public boolean isDepartmentFilterValid() {
        return !Boolean.TRUE.equals(unassigned) || deptId == null;
    }
}
