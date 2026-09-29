package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统部门表数据库访问层
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {
    /** 查询当前租户有效部门及其沿有效父子链可达的子部门ID。 */
    Set<Long> selectActiveDescendantIds(@Param("deptId") Long deptId,
                                        @Param("tenantId") Long tenantId);
}

