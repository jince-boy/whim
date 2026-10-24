package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysDictData;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jince
 * @date 2026/10/24
 * @description 系统字典数据持久化。
 */
@Mapper
public interface SysDictDataMapper extends BaseMapper<SysDictData> {
}
