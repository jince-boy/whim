package com.whim.system.model.entity;

import com.whim.mybatis.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Jince
 * date 2024-10-23 19:53:10
 * description: 菜单权限(SysPermission)实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysPermission extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -18778024102398442L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 权限/菜单名称
     */
    private String name;
    /**
     * 父菜单id
     */
    private Long parentId;
    /**
     * 类型(1-目录 2-菜单 3-按钮 4-外链)
     */
    private Integer type;
    /**
     * 前端路由路径
     */
    private String path;
    /**
     * 前端组件路径
     */
    private String component;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 权限标识
     */
    private String code;
    /**
     * 显示状态
     */
    private Integer visible;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 菜单图标
     */
    private String icon;
    /**
     * 跳转地址
     */
    private String redirect;
    /**
     * 备注
     */
    private String remark;
    /**
     * 逻辑删除
     */
    private Integer deleted;
}
