package com.whim.system.model.entity;

import com.whim.mybatis.core.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Jince
 * date 2024-10-23 17:45:59
 * @description 系统用户(SysUser)实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysUser extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -57027048161786815L;
    /**
     * 主键
     */
    private Long id;
    /**
     * 部门ID
     */
    private Long deptId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 头像
     */
    private String avatar;
    /**
     * 名称
     */
    private String name;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 性别
     */
    private Integer gender;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;
    /**
     * 删除状态
     */
    private Integer deleted;
}
