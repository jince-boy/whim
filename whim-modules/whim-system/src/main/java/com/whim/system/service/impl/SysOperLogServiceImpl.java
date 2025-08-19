package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.utils.ConvertUtils;
import com.whim.log.event.OperLogEvent;
import com.whim.system.mapper.SysOperLogMapper;
import com.whim.system.model.entity.SysOperLog;
import com.whim.system.service.ISysOperLogService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 操作日志(SysOperLog)表服务实现类
 *
 * @author Jince
 * @since 2025-08-19 21:46:45
 */
@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements ISysOperLogService {
    /**
     * 处理操作日志
     *
     * @param operLogEvent 操作日志事件
     */
    @Async
    @EventListener
    @Override
    public void handleOperLog(OperLogEvent operLogEvent) {
        this.save(ConvertUtils.convert(operLogEvent, SysOperLog.class));
    }
}

