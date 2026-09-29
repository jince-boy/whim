package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysRoleDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统角色部门关联表数据库访问层
 */
@Mapper
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {
    /** 查询角色当前仍有效且属于指定租户的自定义部门ID。 */
    Set<Long> selectActiveDepartmentIds(@Param("roleId") Long roleId,
                                        @Param("tenantId") Long tenantId);

    /** 创建或恢复当前租户的角色部门绑定。 */
    void upsertBinding(@Param("id") Long id, @Param("roleId") Long roleId,
                       @Param("deptId") Long deptId, @Param("tenantId") Long tenantId,
                       @Param("operatorId") Long operatorId);
}

