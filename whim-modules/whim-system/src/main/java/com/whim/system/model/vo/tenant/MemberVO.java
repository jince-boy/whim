package com.whim.system.model.vo.tenant;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/28
 * @description 租户成员响应，不含全局账号敏感字段。
 */
@Data
public class MemberVO {
    private Long userId;
    private String username;
    private String name;
    private Integer userStatus;
    private Integer memberStatus;
    private Long deptId;
}
