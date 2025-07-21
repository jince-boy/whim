package com.whim.system.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.whim.system.model.entity.SysPermission;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Jince
 * @date: 2025/7/21 12:48
 * @description: 菜单VO
 */
@Data
@AutoMapper(target = SysPermission.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MenuVO {
    /**
     * 菜单名称
     */
    private String name;
    /**
     * 前端路由路径
     */
    private String path;
    /**
     * 路由参数
     */
    private Map<String, Object> queryParam;
    /**
     * 前端组件路径
     */
    private String component;
    /**
     * 菜单图标
     */
    private String icon;
    /**
     * 显示状态
     */
    private Integer visible;
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
    private List<MenuVO> children;
}
