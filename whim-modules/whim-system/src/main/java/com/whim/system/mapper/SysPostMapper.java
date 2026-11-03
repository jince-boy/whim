package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.core.auth.model.PostInfo;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.system.model.entity.SysPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/03
 * @description 岗位持久化及用户任职信息查询。
 */
@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {
    /**
     * 登录时读取用户关联的有效岗位，部门有效性在构建上下文时统一核验。
     *
     * @param userId 用户ID
     * @return 用户关联的有效岗位信息
     */
    @DataPermissionMetadata
    List<PostInfo> getPostInfoListByUserId(@Param("userId") Long userId);
}
