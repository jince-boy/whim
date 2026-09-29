package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.dto.permission.DataScopeDecisionDTO;
import com.whim.system.model.entity.SysPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author jince
 * @date 2026/07/02
 * @description 系统岗位表数据库访问层
 */
@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {
    /** 检查当前租户岗位编码是否已占用，软删记录仍占用唯一编码。 */
    boolean existsTenantPostCode(@Param("tenantId") Long tenantId, @Param("postCode") String postCode,
                                 @Param("excludedPostId") Long excludedPostId);

    /** 按当前租户与本次删除范围软删未关联成员的岗位。 */
    int softDeleteScoped(@Param("postId") Long postId, @Param("scope") DataScopeDecisionDTO scope);
}

