package com.whim.system.model.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author jince
 * @date 2026/06/30
 * @description 角色与部门关联表实体类
 */
@Data
public class SysRoleDept implements Serializable {
    @Serial
    private static final long serialVersionUID = 294357337651206652L;

    /**
     *
     */
    @TableId(value = "id")
    private Long id;

    /**
     *
     */
    private Long roleId;

    /**
     *
     */
    private Long deptId;

    /**
     *
     */
    private Long createBy;

    /**
     *
     */
    private LocalDateTime createTime;

    /**
     *
     */
    private Long updateBy;

    /**
     *
     */
    private LocalDateTime updateTime;

}

