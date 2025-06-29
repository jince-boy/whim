package com.whim.system.model.entity;

import com.whim.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统字典表(SysDictType)实体类
 *
 * @author Jince
 * @since 2025-06-27 17:23:37
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysDictType extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 414231412681793915L;
    /**
     * 字典ID
     */
    private Long id;
    /**
     * 字典名称
     */
    private String name;
    /**
     * 字典类型(唯一标识)
     */
    private String type;
    /**
     * 状态(0-启用 1-禁用)
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;
}
