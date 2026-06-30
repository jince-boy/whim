package com.whim.system.model.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/06/30
 * @description 岗位表实体类
 */
@Data
public class SysPost implements Serializable {
    @Serial
    private static final long serialVersionUID = 101321624296029639L;

    /**
     * 岗位ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 岗位编码
     */
    private String code;

    /**
     * 岗位名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态(0启用，1禁用)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    private Long updateBy;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志(0-未删除 1-已删除)
     */
    private Integer deleted;

}

