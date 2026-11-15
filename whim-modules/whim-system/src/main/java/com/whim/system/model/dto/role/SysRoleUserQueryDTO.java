package com.whim.system.model.dto.role;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/15
 * @description 角色已分配用户和候选用户的分页查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRoleUserQueryDTO extends PageQueryDTO {
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
    @Size(max = 32, message = "用户名不能超过32个字符")
    private String username;
    @Size(max = 24, message = "姓名不能超过24个字符")
    private String name;
}
