package com.whim.core.permission;

import com.whim.core.exception.DataAccessDeniedException;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 同一操作的角色数据范围并集，业务归属与创建审计分别表达。
 */
@Data
public class DataPermissionScope {
    private Long userId;
    private Long userDepartmentId;
    /** 对应权限定义明确启用了数据范围，防止错误配置静默放行。 */
    private boolean dataProtected;
    private boolean all;
    private boolean self;
    private Set<Long> departmentIds = new LinkedHashSet<>();

    /** 判断目标归属人或部门是否位于本次操作的允许范围。 */
    public boolean allows(Long ownerUserId, Long departmentId) {
        return all || departmentIds.contains(departmentId)
                || (self && Objects.equals(userId, ownerUserId));
    }

    /** 校验详情或写入数据的业务归属，不能用创建人代替负责人。 */
    public void checkOwnership(Long ownerUserId, Long departmentId) {
        if (!allows(ownerUserId, departmentId)) {
            throw new DataAccessDeniedException("目标数据不在本次操作的授权范围内");
        }
    }

    /** 校验部门节点或部门迁移目标，纯本人范围不能管理任意部门。 */
    public void checkDepartment(Long departmentId) {
        if (!all && !departmentIds.contains(departmentId)) {
            throw new DataAccessDeniedException("目标部门不在本次操作的授权范围内");
        }
    }
}
