package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jince
 * date 2024-10-23 17:47:08
 * description: 系统用户(SysUser)表数据库访问层
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

}

