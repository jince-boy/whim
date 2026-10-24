package com.whim.system.model.entity;


import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统权限菜单；可空编辑字段按完整编辑结果写入，状态变更使用已加载实体。
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
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String routeName;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 权限标识
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String perms;

    /** 内置定义不能改变授权语义；核心管理节点以外允许删除。 */
    private Integer builtIn;

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
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String path;

    /**
     * 路由参数
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String param;

    /**
     * 前端组件路径
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
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
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
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
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String remark;

    /**
     * 删除标识：0表示未删除，已删除时为本行主键ID
     */
    @TableLogic
    private Long deleted;

}

