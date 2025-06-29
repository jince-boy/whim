package com.whim.system.model.entity;

import com.whim.mybatis.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门表(SysDept)实体类
 *
 * @author Jince
 * @since 2025-06-27 16:32:54
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysDept extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -35711403242614581L;
    /**
     * 部门ID
     */
    private Long id;
    /**
     * 父部门ID(0表示根部门)
     */
    private Long parentId;
    /**
     * 祖级列表(逗号分隔，如:0,1,2)
     */
    private String ancestors;
    /**
     * 部门名称
     */
    private String name;
    /**
     * 部门编码
     */
    private String code;
    /**
     * 排序(升序)
     */
    private Integer sort;
    /**
     * 部门负责人ID
     */
    private Long leaderId;
    /**
     * 部门负责人姓名
     */
    private String leaderName;
    /**
     * 联系电话
     */
    private String phone;
    /**
     * 邮箱
     */
    private String email;
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
    private Integer deleted;
}
