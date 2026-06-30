package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author jince
 * @date 2026/06/30
 * @description 字典数据表数据库访问层
 */
@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {
}

