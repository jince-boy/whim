package com.whim.system.model.vo.dept;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 租户部门树节点响应。
 */
@Data
public class DeptVO {
    private Long id;
    private Long parentId;
    private String deptName;
    private Integer sort;
    private Integer status;
}
