package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author jince
 * @date 2026/07/03
 * @description 系统配置表数据库访问层
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
}

