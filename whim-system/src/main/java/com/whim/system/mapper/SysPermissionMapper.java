package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.model.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jince
 * date 2024-10-23 19:53:58
 * description: 菜单权限(SysPermission)表数据库访问层
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

}

