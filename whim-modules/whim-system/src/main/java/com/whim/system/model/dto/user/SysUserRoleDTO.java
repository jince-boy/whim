package com.whim.system.model.dto.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jince
 * @date 2026/11/06
 * @description 用户角色分配参数。
 */
@Data
public class SysUserRoleDTO {
    /** 用户ID。 */
    @NotNull(message = "用户ID不能为空")
    private Long id;

    /** 完整角色集合，空集合表示清空可管理的角色。 */
    @NotNull(message = "角色集合不能为空")
    @Size(max = 100, message = "最多选择100个角色")
    private List<@NotNull(message = "角色ID不能为空") Long> roleIds = new ArrayList<>();
}
