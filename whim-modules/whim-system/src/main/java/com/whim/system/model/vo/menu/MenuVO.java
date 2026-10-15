package com.whim.system.model.vo.menu;

import lombok.Data;
import com.whim.system.model.entity.SysPermission;
import io.github.linpeilie.annotations.AutoMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jince
 * @date 2026/10/13
 * @description 菜单树节点；按钮权限仅出现在管理目录，不出现在用户导航。
 */
@Data
@AutoMapper(target = SysPermission.class, convertGenerate = false)
public class MenuVO {
    private Long id;
    private Long parentId;
    private String menuName;
    private Integer menuType;
    private String routeName;
    private String path;
    private String component;
    private String param;
    private String perms;
    private Integer dataPermission;
    private Integer builtIn;
    /** 核心管理节点不能删除、停用、隐藏或迁移。 */
    private Boolean protectedNode;
    private Integer isCache;
    private Integer sort;
    private String icon;
    private Integer visible;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private List<MenuVO> children = new ArrayList<>();
}
