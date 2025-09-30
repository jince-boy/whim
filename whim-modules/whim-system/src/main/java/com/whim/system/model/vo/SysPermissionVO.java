package com.whim.system.model.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.whim.system.model.entity.SysPermission;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Jince
 * @date: 2025/7/21 12:48
 * @description: 菜单VO
 */
@Data
@AutoMapper(target = SysPermission.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SysPermissionVO {
    /**
     * 菜单id
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 父菜单id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;
    /**
     * 类型(1-目录 2-菜单 3-按钮 4-外链)
     */
    private Integer type;
    /**
     * 权限标识
     */
    private String code;
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
     * 菜单图标
     */
    private String icon;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 显示状态
     */
    private Integer visible;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 跳转地址
     */
    private String redirect;
    /**
     * 备注
     */
    private String remark;
    /**
     * 子菜单
     */
    private List<SysPermissionVO> children;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
