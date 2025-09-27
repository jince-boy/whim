package com.whim.system.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.whim.system.model.entity.SysRole;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * @author Jince
 * @date: 2025/7/22 00:17
 * @description: 角色VO
 */
@Data
@AutoMapper(target = SysRole.class)
public class SysRoleVO {
    /**
     * 主键
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
}
