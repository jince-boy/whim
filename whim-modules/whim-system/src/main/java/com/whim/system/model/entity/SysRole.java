package com.whim.system.model.entity;

import com.whim.mybatis.core.model.entity.BaseEntity;
import com.whim.satoken.core.model.RoleInfo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Jince
 * date 2024-10-23 19:53:10
 * description: 系统角色(SysRole)实体类
 */
@AutoMapper(target = RoleInfo.class)
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
     * 数据权限范围(1-全部 2-本部门及以下 3-本部门 4-自定义 5-仅本人)
     */
    private Integer dataScope;
    /**
     * 角色描述
     */
    private String description;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 逻辑删除字段
     */
    private Integer deleted;
}
