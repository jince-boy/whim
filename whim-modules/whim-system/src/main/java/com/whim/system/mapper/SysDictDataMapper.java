package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;

/**
 * 字典数据表(SysDictData)表数据库访问层
 *
 * @author Jince
 * @since 2025-06-27 17:23:36
 */
@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {

}

