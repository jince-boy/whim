package com.whim.system.model.dto.sysPermission;

import com.whim.system.model.entity.SysPermission;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * @author jince
 * date: 2025/9/20 22:20
 * description: 添加菜单参数
 */
@Data
@AutoMapper(target = SysPermission.class)
public class SysPermissionInsertDTO {
    /**
     * 路由名称
     */
    private String name;
    /**
     * 菜单名称
     */
    private String title;
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
     * 路由参数
     */
    private String queryParam;
    /**
     * 前端组件路径
     */
    private String component;
    /**
     * 是否缓存
     */
    private Integer keepAlive;
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
}
