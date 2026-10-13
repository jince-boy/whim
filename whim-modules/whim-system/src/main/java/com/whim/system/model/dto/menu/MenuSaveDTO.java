package com.whim.system.model.dto.menu;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import com.whim.system.model.entity.SysPermission;
import io.github.linpeilie.annotations.AutoMapper;

/**
 * @author Jince
 * @date 2026/10/13
 * @description 菜单和操作权限的完整编辑参数，内置标志由服务端维护。
 */
@Data
@AutoMapper(target = SysPermission.class, reverseConvertGenerate = false)
public class MenuSaveDTO {
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 64, message = "菜单名称不能超过64个字符")
    private String menuName;
    @NotNull(message = "上级菜单不能为空")
    @Min(value = 0, message = "上级菜单ID不能为负数")
    private Long parentId;
    @NotNull(message = "菜单类型不能为空")
    @Min(value = 1, message = "菜单类型只能为1至4")
    @Max(value = 4, message = "菜单类型只能为1至4")
    private Integer menuType;
    @Size(max = 128, message = "路由名称不能超过128个字符")
    private String routeName;
    @Size(max = 255, message = "路径不能超过255个字符")
    private String path;
    @Size(max = 255, message = "组件路径不能超过255个字符")
    private String component;
    @Size(max = 255, message = "路由参数不能超过255个字符")
    private String param;
    @Size(max = 128, message = "权限标识不能超过128个字符")
    private String perms;
    @NotNull(message = "数据权限标志不能为空")
    @Min(value = 0, message = "数据权限标志只能为0或1")
    @Max(value = 1, message = "数据权限标志只能为0或1")
    private Integer dataPermission;
    @NotNull(message = "缓存标志不能为空")
    @Min(value = 0, message = "缓存标志只能为0或1")
    @Max(value = 1, message = "缓存标志只能为0或1")
    private Integer isCache;
    @NotNull(message = "排序不能为空")
    @Min(value = 0, message = "排序不能为负数")
    @Max(value = 9999, message = "排序不能超过9999")
    private Integer sort;
    @Size(max = 128, message = "图标不能超过128个字符")
    private String icon;
    @NotNull(message = "可见性不能为空")
    @Min(value = 0, message = "可见性只能为0或1")
    @Max(value = 1, message = "可见性只能为0或1")
    private Integer visible;
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    private Integer status;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
