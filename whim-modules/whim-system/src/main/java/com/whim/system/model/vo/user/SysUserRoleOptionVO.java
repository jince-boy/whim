package com.whim.system.model.vo.user;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/06
 * @description 用户角色分配的候选角色。
 */
@Data
public class SysUserRoleOptionVO {
    /** 角色ID。 */
    private Long id;
    /** 角色名称。 */
    private String roleName;
    /** 角色编码。 */
    private String roleCode;
    /** 角色状态。 */
    private Integer status;
    /** 当前操作者是否不能调整此角色。 */
    private boolean disabled;
}
