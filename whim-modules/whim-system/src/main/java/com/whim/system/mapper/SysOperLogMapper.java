package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author jince
 * @date 2026/06/30
 * @description 操作日志数据库访问层
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
}

