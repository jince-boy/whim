package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.model.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jince
 * date 2024-10-23 19:53:58
 * description: 系统角色(SysRole)表数据库访问层
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

}

