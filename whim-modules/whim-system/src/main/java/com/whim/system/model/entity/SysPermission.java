package com.whim.system.model.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/06/30
 * @description 权限表(菜单和按钮权限)实体类
 */
@Data
public class SysPermission implements Serializable {
    @Serial
    private static final long serialVersionUID = 533411026337325087L;

    /**
     * 权限ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 路由名称
     */
    private String name;

    /**
     * 菜单名称
     */
    private String title;

    /**
     * 父权限ID(0表示根节点)
     */
    private Long parentId;

    /**
     * 类型(1-目录 2-菜单 3-按钮 4-外链)
     */
    private Integer type;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 路由参数
     */
    private String queryParam;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 页面是否缓存(0不缓存，1缓存)
     */
    private Integer keepAlive;

    /**
     * 显示排序(升序)
     */
    private Integer sort;

    /**
     * 权限标识(如:system:user:add)
     */
    @TableField(value = "perms")
    private String code;

    /**
     * 是否可见(0-显示 1-隐藏)
     */
    private Integer visible;

    /**
     * 状态(0-启用 1-禁用)
     */
    private Integer status;

    /**
     * 图标名称
     */
    private String icon;

    /**
     * 重定向路径
     */
    private String redirect;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志(0-未删除 1-已删除)
     */
    private Integer deleted;

}

