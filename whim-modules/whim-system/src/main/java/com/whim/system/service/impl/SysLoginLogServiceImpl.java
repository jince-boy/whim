package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.system.mapper.SysLoginLogMapper;
import com.whim.system.model.entity.SysLoginLog;
import com.whim.system.service.ISysLoginLogService;
import org.springframework.stereotype.Service;

/**
 * @author jince
 * @date 2026/06/30
 * @description 系统登录日志服务实现类
 */
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements ISysLoginLogService {
}

