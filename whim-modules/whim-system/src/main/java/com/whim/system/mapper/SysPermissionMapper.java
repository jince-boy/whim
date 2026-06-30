package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author jince
 * @date 2026/06/30
 * @description 权限表(菜单和按钮权限)数据库访问层
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
}

