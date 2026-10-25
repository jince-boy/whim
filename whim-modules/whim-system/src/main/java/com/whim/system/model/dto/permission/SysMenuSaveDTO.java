package com.whim.system.model.dto.permission;

import com.whim.system.model.entity.SysPermission;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 菜单新增和完整修改参数，不接受内置标志、审计字段和删除标识。
 */
@Data
@AutoMapper(target = SysPermission.class, reverseConvertGenerate = false)
public class SysMenuSaveDTO {
    /** 上级菜单ID，0表示顶级。 */
    @NotNull(message = "上级菜单不能为空")
    @PositiveOrZero(message = "上级菜单ID不能小于0")
    private Long parentId;

    /** 菜单名称。 */
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 64, message = "菜单名称不能超过64个字符")
    private String menuName;

    /** 类型：1目录、2页面、3按钮、4外链。 */
    @NotNull(message = "菜单类型不能为空")
    @Min(value = 1, message = "菜单类型无效")
    @Max(value = 4, message = "菜单类型无效")
    private Integer menuType;

    /** 目录和页面的唯一路由名称。 */
    @Size(max = 128, message = "路由名称不能超过128个字符")
    private String routeName;

    /** 前端路由路径或外链地址，格式由前端校验。 */
    @Size(max = 255, message = "路由地址不能超过255个字符")
    private String path;

    /** 页面组件路径，由前端解析。 */
    @Size(max = 255, message = "组件路径不能超过255个字符")
    private String component;

    /** 页面查询参数，例如tab=basic。 */
    @Size(max = 255, message = "路由参数不能超过255个字符")
    private String param;

    /** 页面和按钮的可选权限标识，留空时不授予接口操作权限。 */
    @Size(max = 128, message = "权限标识不能超过128个字符")
    private String perms;

    /** 页面缓存：0不缓存、1缓存。 */
    @NotNull(message = "缓存状态不能为空")
    @Min(value = 0, message = "缓存状态无效")
    @Max(value = 1, message = "缓存状态无效")
    private Integer isCache = 0;

    /** 同级排序值。 */
    @NotNull(message = "排序值不能为空")
    @PositiveOrZero(message = "排序值不能小于0")
    private Integer sort = 0;

    /** 图标名称。 */
    @Size(max = 128, message = "图标名称不能超过128个字符")
    private String icon;

    /** 显示状态：0显示、1隐藏。 */
    @NotNull(message = "显示状态不能为空")
    @Min(value = 0, message = "显示状态无效")
    @Max(value = 1, message = "显示状态无效")
    private Integer visible = 0;

    /** 运行状态：0正常、1停用。 */
    @NotNull(message = "运行状态不能为空")
    @Min(value = 0, message = "运行状态无效")
    @Max(value = 1, message = "运行状态无效")
    private Integer status = 0;

    /** 备注。 */
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
