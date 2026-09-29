package com.whim.system.model.dto.tenant;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 租户成员主部门分配参数，空值表示取消归属。
 */
@Data
public class MemberDepartmentDTO {
    private Long deptId;
}
