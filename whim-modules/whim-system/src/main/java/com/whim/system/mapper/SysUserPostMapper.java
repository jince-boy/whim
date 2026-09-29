package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysUserPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统用户岗位关联表数据库访问层
 */
@Mapper
public interface SysUserPostMapper extends BaseMapper<SysUserPost> {
    /** 创建或恢复当前租户的岗位成员绑定。 */
    void upsertBinding(@Param("id") Long id, @Param("userId") Long userId,
                       @Param("postId") Long postId, @Param("tenantId") Long tenantId,
                       @Param("operatorId") Long operatorId);

    /** 软删除当前岗位不再关联的成员并填写删除审计字段。 */
    int removeExcludedMembers(@Param("postId") Long postId, @Param("tenantId") Long tenantId,
                              @Param("userIds") Set<Long> userIds, @Param("operatorId") Long operatorId);
}

