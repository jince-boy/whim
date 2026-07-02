package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统角色表数据库访问层
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /**
     * 查询用户已启用角色编码列表。
     *
     * @param userId 用户ID
     * @return 角色编码集合
     */
    Set<String> selectRoleCodeSetByUserId(@Param("userId") Long userId);
}

