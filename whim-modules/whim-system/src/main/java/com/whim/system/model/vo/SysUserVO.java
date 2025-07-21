package com.whim.system.model.vo;

import com.whim.system.model.entity.SysUser;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.List;

/**
 * @author Jince
 * @date: 2025/7/21 12:26
 * @description: 系统用户VO
 */
@Data
@AutoMapper(target = SysUser.class)
public class SysUserVO {
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
     * 角色列表
     */
    private List<SysRoleVO> roles;
}
