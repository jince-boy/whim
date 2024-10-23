package com.whim.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.whim.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Jince
 * date 2024-10-23 17:45:59
 * description: 系统用户(SysUser)实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysUser extends BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = -57027048161786815L;
    /**
     * 主键
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
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
    private String mobile;
    /**
     * 性别
     */
    private String gender;
    /**
     * 状态
     */
    private String status;
}
