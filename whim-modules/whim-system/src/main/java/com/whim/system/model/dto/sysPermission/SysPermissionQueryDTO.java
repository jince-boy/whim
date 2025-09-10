package com.whim.system.model.dto.sysPermission;

import com.whim.system.model.entity.SysPermission;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * @author jince
 * date: 2025/9/9 22:16
 * description: 菜单查询参数
 */
@Data
@AutoMapper(target = SysPermission.class)
public class SysPermissionQueryDTO {
    private String title;
}
