package com.whim.core.auth.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 当前登录用户的有效岗位信息，岗位不授予角色或权限。
 */
@Data
public class PostInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 岗位ID。 */
    private Long postId;

    /** 岗位编码。 */
    private String postCode;

    /** 岗位名称。 */
    private String postName;

    /** 岗位所属部门ID，空表示全组织岗位。 */
    private Long deptId;
}
