package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志(SysOperLog)表数据库访问层
 *
 * @author Jince
 * @since 2025-08-19 21:46:44
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

}

