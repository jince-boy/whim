package com.whim.system.model.vo.user;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 用户管理响应，永远不返回密码及认证凭证。
 */
@Data
public class UserVO {
    private Long id;
    private String username;
    private String name;
    private String avatar;
    private String email;
    private String phone;
    private Integer gender;
    private Long deptId;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}
