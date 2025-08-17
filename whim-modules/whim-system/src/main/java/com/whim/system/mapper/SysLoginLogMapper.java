package com.whim.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whim.system.model.entity.SysLoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统登录日志(SysLoginLog)表数据库访问层
 *
 * @author Jince
 * @since 2025-08-17 17:15:29
 */
@Mapper
public interface SysLoginLogMapper extends BaseMapper<SysLoginLog> {

}

