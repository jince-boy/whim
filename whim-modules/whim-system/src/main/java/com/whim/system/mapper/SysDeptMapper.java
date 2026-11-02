package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.system.model.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/11/02
 * @description 部门持久化接口。
 */
@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {
    /**
     * 查询因祖先部门新增有效下级而扩大数据范围的用户，供事务提交后失效会话。
     *
     * @param ancestorIds 新增部门的祖先ID集合，不包含根标识0
     * @return 拥有本部门及下级范围角色的受影响用户ID集合
     */
    @DataPermissionMetadata
    Set<Long> getUserIdsByDepartmentAncestors(@Param("ancestorIds") Set<Long> ancestorIds);
}
