package com.whim.system.model.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/06/30
 * @description 系统字典表实体类
 */
@Data
public class SysDictType implements Serializable {
    @Serial
    private static final long serialVersionUID = 608323066902496727L;

    /**
     * 字典ID
     */
    @TableId(value = "id")
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

