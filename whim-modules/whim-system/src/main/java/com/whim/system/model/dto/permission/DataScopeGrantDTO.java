package com.whim.system.model.dto.permission;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 实际授予某项操作的角色及其默认或覆盖数据范围。
 */
@Data
public class DataScopeGrantDTO {
    private Long roleId;
    private Long permissionId;
    private Integer dataScope;
    private Boolean overridden;
    private boolean dataPermission;
}
