package com.whim.system.model.vo.user;

import com.whim.system.model.entity.SysUser;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/06
 * @description 用户列表信息，不包含密码或密码哈希。
 */
@Data
@AutoMapper(target = SysUser.class, convertGenerate = false)
public class SysUserVO {
    /** 用户ID。 */
    private Long id;
    /** 用户名。 */
    private String username;
    /** 超级管理员账号身份，不通过角色授予。 */
    private boolean superAdministrator;
    /** 姓名。 */
    private String name;
    /** 头像地址，未设置时为null。 */
    private String avatar;
    /** 主部门ID。 */
    private Long deptId;
    /** 主部门名称。 */
    private String deptName;
    /** 邮箱。 */
    private String email;
    /** 手机号码。 */
    private String phone;
    /** 性别字典编码。 */
    private Integer gender;
    /** 用户状态。 */
    private Integer status;
    /** 备注。 */
    private String remark;
    /** 创建时间，由持久化层自动填充。 */
    private LocalDateTime createTime;
}
