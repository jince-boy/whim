package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.core.auth.model.RoleInfo;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.system.model.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 登录账号的有效角色查询。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /** 查询账号当前有效角色。 */
    @DataPermissionMetadata
    List<RoleInfo> getRoleInfoListByUserId(@Param("userId") Long userId);
}

