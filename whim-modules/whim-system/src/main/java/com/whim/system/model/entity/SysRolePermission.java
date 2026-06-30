package com.whim.system.model.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/06/30
 * @description 角色-权限关联表实体类
 */
@Data
public class SysRolePermission implements Serializable {
    @Serial
    private static final long serialVersionUID = -78327119827786473L;

    /**
     * 关联ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 权限ID
     */
    private Long permissionId;

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

}

