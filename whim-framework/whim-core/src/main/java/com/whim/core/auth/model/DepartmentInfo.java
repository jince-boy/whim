package com.whim.core.auth.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 当前登录用户的有效主部门信息。
 */
@Data
public class DepartmentInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 部门ID。 */
    private Long deptId;

    /** 父部门ID，0表示根节点。 */
    private Long parentId;

    /** 部门名称。 */
    private String deptName;

    /** 部门负责人ID。 */
    private Long leaderUserId;
}
