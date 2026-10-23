package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.core.auth.model.PostInfo;
import com.whim.mybatisplus.annotation.DataPermissionMetadata;
import com.whim.system.model.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Jince
 * @date 2026/09/30
 * @description 系统用户持久化。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    /** 登录时读取用户关联的有效岗位，部门有效性在构建上下文时统一核验。 */
    @DataPermissionMetadata
    List<PostInfo> getPostInfoListByUserId(@Param("userId") Long userId);
}

