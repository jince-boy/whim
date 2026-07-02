package com.whim.system.model.entity;


import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统字典表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysDictType extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -63631833626331945L;

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
     * 备注
     */
    private String remark;

}

