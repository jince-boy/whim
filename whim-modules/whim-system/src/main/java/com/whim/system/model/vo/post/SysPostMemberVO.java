package com.whim.system.model.vo.post;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/01
 * @description 岗位成员和可选用户的最小必要信息。
 */
@Data
public class SysPostMemberVO {
    /** 用户ID。 */
    private Long userId;
    /** 用户名。 */
    private String username;
    /** 姓名。 */
    private String name;
    /** 用户主部门ID。 */
    private Long deptId;
    /** 用户状态：0正常、1停用。 */
    private Integer status;
}
