package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysUserTwoFactor;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 用户两步验证凭据持久化。
 */
@Mapper
public interface SysUserTwoFactorMapper extends BaseMapper<SysUserTwoFactor> {
}
