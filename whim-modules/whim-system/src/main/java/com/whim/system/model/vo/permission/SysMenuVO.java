package com.whim.system.model.vo.permission;

import com.whim.system.model.entity.SysPermission;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 菜单详情、管理树及前端动态路由使用的菜单节点。
 */
@Data
@AutoMapper(target = SysPermission.class, convertGenerate = false)
public class SysMenuVO {
    /** 菜单ID。 */
    private Long id;
    /** 上级菜单ID。 */
    private Long parentId;
    /** 菜单名称。 */
    private String menuName;
    /** 类型：1目录、2页面、3按钮、4外链。 */
    private Integer menuType;
    /** 路由名称。 */
    private String routeName;
    /** 路由地址或外链。 */
    private String path;
    /** 页面组件路径。 */
    private String component;
    /** 页面查询参数。 */
    private String param;
    /** 精确权限码。 */
    private String perms;
    /** 缓存状态。 */
    private Integer isCache;
    /** 同级排序值。 */
    private Integer sort;
    /** 图标。 */
    private String icon;
    /** 显示状态：0显示、1隐藏。 */
    private Integer visible;
    /** 运行状态：0正常、1停用。 */
    private Integer status;
    /** 内置标志。 */
    private Integer builtIn;
    /** 是否为不能删除或停用的核心管理节点。 */
    private boolean protectedNode;
    /** 备注。 */
    private String remark;
    /** 创建时间。 */
    private LocalDateTime createTime;
    /** 更新时间。 */
    private LocalDateTime updateTime;
    /** 子节点，按sort、id升序。 */
    private List<SysMenuVO> children = new ArrayList<>();
}
