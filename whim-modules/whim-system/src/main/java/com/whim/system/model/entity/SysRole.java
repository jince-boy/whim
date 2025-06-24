package com.whim.system.model.entity;

import com.whim.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Jince
 * date 2024-10-23 19:53:10
 * description: 系统角色(SysRole)实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysRole extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 732410804028084659L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 角色名称
     */
    private String name;
    /**
     * 角色权限编码
     */
    private String code;
    /**
     * 角色描述
     */
    private String description;
    /**
     * 状态
     */
    private String status;
}
