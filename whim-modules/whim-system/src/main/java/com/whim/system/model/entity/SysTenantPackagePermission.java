package com.whim.system.model.entity;


import com.baomidou.mybatisplus.annotation.TableLogic;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统租户套餐权限关联表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysTenantPackagePermission extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 825374156611654521L;

    /**
     * id
     */
    private Long id;

    /**
     * 租户套餐ID
     */
    private Long packageId;

    /**
     * 权限ID
     */
    private Long permissionId;

    /**
     * 删除标志(0-未删除 1-已删除)
     */
    @TableLogic
    private Integer deleted;

}

