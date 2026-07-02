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
 * @description 系统岗位表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPost extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 666746659954323077L;

    /**
     * id
     */
    private Long id;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 岗位名称
     */
    private String postName;

    /**
     * 岗位编码
     */
    private String postCode;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态(0-正常 1-停用)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 删除标志(0-未删除 1-已删除)
     */
    @TableLogic
    private Integer deleted;

}

