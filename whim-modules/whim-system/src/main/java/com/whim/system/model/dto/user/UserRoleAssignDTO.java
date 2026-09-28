package com.whim.system.model.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/28
 * @description 成员角色覆盖式分配参数。
 */
@Data
public class UserRoleAssignDTO {
    @NotNull(message = "角色ID集合不能为空")
    private Set<@NotNull(message = "角色ID不能为空") Long> roleIds;
}
