package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysDictType;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统字典表(SysDictType)表数据库访问层
 *
 * @author Jince
 * @since 2025-06-27 17:23:37
 */
@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {

}

