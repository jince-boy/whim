package com.whim.system.model.vo.role;

import com.whim.system.model.entity.SysRole;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/28
 * @description 系统角色响应。
 */
@Data
@AutoMapper(target = SysRole.class, convertGenerate = false)
public class RoleVO {
    private Long id;
    private String roleName;
    private String roleCode;
    private Integer dataScope;
    private Integer status;
}
