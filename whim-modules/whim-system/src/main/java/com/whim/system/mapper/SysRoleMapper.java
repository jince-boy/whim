package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whim.core.auth.model.RoleInfo;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.system.model.dto.role.SysRoleUserQueryDTO;
import com.whim.system.model.entity.SysRole;
import com.whim.system.model.vo.role.SysRoleUserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 角色资料、成员分页及登录角色查询。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    /**
     * 按角色关联关系分页查询已分配或候选用户。
     *
     * @param page 分页参数
     * @param query 角色ID及用户筛选条件
     * @param assigned 是否查询已分配用户
     * @param currentUserId 当前登录用户ID
     * @return 经过操作级数据权限过滤的用户分页
     */
    Page<SysRoleUserVO> getRoleUserPage(Page<SysRoleUserVO> page,
                                      @Param("query") SysRoleUserQueryDTO query,
                                      @Param("assigned") boolean assigned,
                                      @Param("currentUserId") Long currentUserId);

    /**
     * 查询账号当前有效角色。
     *
     * @param userId 用户ID
     * @return 正常且未删除的角色
     */
    @DataPermissionMetadata
    List<RoleInfo> getRoleInfoListByUserId(@Param("userId") Long userId);
}

