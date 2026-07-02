package com.whim.system.model.entity;


import com.baomidou.mybatisplus.annotation.TableLogic;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统权限菜单表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPermission extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 568214924677591964L;

    /**
     * id
     */
    private Long id;

    /**
     * 路由名称
     */
    private String routeName;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 权限标识
     */
    private String perms;

    /**
     * 父级ID(0为顶级菜单)
     */
    private Long parentId;

    /**
     * 类型(1-目录 2-菜单 3-按钮 4-外链)
     */
    private Integer menuType;

    /**
     * 前端路由路径
     */
    private String path;

    /**
     * 路由参数
     */
    private String param;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 是否缓存(0-不缓存 1-缓存)
     */
    private Integer isCache;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 图标
     */
    private String icon;

    /**
     * 可见性(0-显示 1-隐藏)
     */
    private Integer visible;

    /**
     * 状态(0-正常 1-停用)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标志(0-未删除 1-已删除)
     */
    @TableLogic
    private Integer deleted;

}

