package com.whim.system.model.vo.role;

import com.whim.system.model.entity.SysRole;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/14
 * @description 角色列表和基础资料详情。
 */
@Data
@AutoMapper(target = SysRole.class, convertGenerate = false)
public class SysRoleVO {
    private Long id;
    private String roleName;
    private String roleCode;
    private Integer dataScope;
    private Integer sort;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
