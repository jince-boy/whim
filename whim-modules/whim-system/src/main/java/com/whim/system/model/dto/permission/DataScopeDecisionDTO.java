package com.whim.system.model.dto.permission;

import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 当前租户和动作的有效数据范围，供业务查询与写入校验使用。
 */
@Data
public class DataScopeDecisionDTO {
    private Long tenantId;
    private Long userId;
    private boolean all;
    private boolean self;
    private Set<Long> deptIds = new LinkedHashSet<>();
}
