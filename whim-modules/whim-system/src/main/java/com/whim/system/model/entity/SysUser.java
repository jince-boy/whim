package com.whim.system.model.entity;


import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.whim.core.auth.model.UserInfo;
import com.whim.mybatisplus.model.entity.BaseEntity;
import com.whim.system.model.enums.SysUserStatus;
import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMapping;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Jince
 * @date 2026/07/02
 * @description 系统用户表实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = UserInfo.class, reverseConvertGenerate = false)
public class SysUser extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 612223036732363306L;

    /**
     * id
     */
    @AutoMapping(target = "userId")
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /** 超级管理员账号身份，仅由系统初始化或受控数据库维护设置，业务请求不能修改。 */
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Boolean superAdministrator;

    /**
     * 密码(加密存储)
     */
    private String password;

    /**
     * 头像URL
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String avatar;

    /**
     * 真实姓名
     */
    private String name;

    /**
     * 电子邮箱
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String email;

    /**
     * 手机号码
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String phone;

    /**
     * 性别字典编码
     */
    private Integer gender;

    /**
     * 状态编码，取值见 {@link SysUserStatus}
     */
    private Integer status;

    /** 用户主部门ID。 */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private Long deptId;

    /**
     * 备注
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String remark;

    /**
     * 删除标识：0表示未删除，已删除时为本行主键ID
     */
    @TableLogic
    private Long deleted;

}

