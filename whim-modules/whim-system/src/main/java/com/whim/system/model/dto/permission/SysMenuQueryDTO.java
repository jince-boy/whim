package com.whim.system.model.dto.permission;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 菜单树查询参数。
 */
@Data
public class SysMenuQueryDTO {
    /** 菜单名称，模糊匹配并保留祖先和匹配节点的子树。 */
    @Size(max = 64, message = "菜单名称不能超过64个字符")
    private String menuName;
}
