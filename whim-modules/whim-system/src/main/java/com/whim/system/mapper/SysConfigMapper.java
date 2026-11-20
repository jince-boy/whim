package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 业务配置持久化。
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
}
