package com.whim.system.model.vo.permission;

import lombok.Data;
import com.whim.system.model.entity.SysPermission;
import io.github.linpeilie.annotations.AutoMapper;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 当前用户可访问的导航节点，不返回持久化审计或删除字段。
 */
@Data
@AutoMapper(target = SysPermission.class, convertGenerate = false)
public class UserMenuVO {
    private Long id;
    private Long parentId;
    private String routeName;
    private String menuName;
    private String perms;
    private Integer menuType;
    private String path;
    private String component;
    private String param;
    private Integer isCache;
    private Integer sort;
    private String icon;
    private Integer visible;
    private Integer status;
    private Integer builtIn;
    private boolean protectedNode;
    private String remark;
    private List<UserMenuVO> children = new ArrayList<>();
}

