package com.whim.system.model.vo.user;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jince
 * @date 2026/11/06
 * @description 用户当前角色及允许展示的候选项。
 */
@Data
public class SysUserRolesVO {
    /** 用户ID。 */
    private Long id;
    /** 当前角色ID集合。 */
    private List<Long> roleIds = new ArrayList<>();
    /** 可分配角色及只读的已有角色。 */
    private List<SysUserRoleOptionVO> options = new ArrayList<>();
}
