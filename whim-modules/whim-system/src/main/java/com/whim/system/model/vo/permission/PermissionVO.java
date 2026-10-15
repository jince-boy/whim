package com.whim.system.model.vo.permission;

import com.whim.system.model.entity.SysPermission;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/28
 * @description 功能权限响应。
 */
@Data
@AutoMapper(target = SysPermission.class, convertGenerate = false)
public class PermissionVO {
    private Long id;
    private String menuName;
    private String perms;
    private Integer status;
    private Long parentId;
    private Integer menuType;
    private Integer dataPermission;
}
