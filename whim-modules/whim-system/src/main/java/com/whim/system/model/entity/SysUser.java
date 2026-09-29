package com.whim.system.model.entity;


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
     * 性别字典编码
     */
    private Integer gender;

    /**
     * 状态编码，取值见 {@link SysUserStatus}
     */
    private Integer status;

    /**
     * 默认进入租户ID
     */
    @AutoMapping(ignore = true)
    private Long defaultTenantId;

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

