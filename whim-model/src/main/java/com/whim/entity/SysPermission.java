package com.whim.entity;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.whim.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * @author JinCe
 * date: 2024-06-25 21:56:36
 * description: 菜单权限 SysPermission 实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysPermission extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 728253007362248315L;
    /**
     * 主键
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableId(type = IdType.ASSIGN_ID)
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
     * 菜单类型（1.菜单 2.目录 3.外链 4按钮）
     */
    private String type;
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
    private String permissionCode;
    /**
     * 显示状态
     */
    private String visible;
    /**
     * 状态
     */
    private String status;
    /**
     * 菜单图标
     */
    private String icon;
    /**
     * 备注
     */
    private String remark;
}
