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
 * @description 系统用户表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -34374351238205922L;

    /**
     * 用户ID
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
     * 密码(加密存储)
     */
    private String password;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 真实姓名
     */
    private String name;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 性别(0-未知 1-男 2-女)
     */
    private Integer gender;

    /**
     * 状态(0-启用 1-禁用)
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

