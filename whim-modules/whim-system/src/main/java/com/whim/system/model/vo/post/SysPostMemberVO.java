package com.whim.system.model.vo.post;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/03
 * @description 岗位成员及候选用户，不包含密码或联系方式。
 */
@Data
public class SysPostMemberVO {
    /** 用户ID。 */
    private Long userId;
    /** 用户名。 */
    private String username;
    /** 姓名。 */
    private String name;
    /** 主部门ID，null表示未分配部门。 */
    private Long deptId;
    /** 用户状态：0正常、1停用。 */
    private Integer status;
}
