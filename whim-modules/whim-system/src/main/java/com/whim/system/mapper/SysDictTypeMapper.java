package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysDictType;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author jince
 * @date 2026/06/30
 * @description 系统字典表数据库访问层
 */
@Mapper
public interface SysDictTypeMapper extends BaseMapper<SysDictType> {
}

