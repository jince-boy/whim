package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.log.event.OperLogEvent;
import com.whim.system.model.entity.SysOperLog;

/**
 * 操作日志(SysOperLog)表服务接口
 *
 * @author Jince
 * @since 2025-08-19 21:46:44
 */
public interface ISysOperLogService extends IService<SysOperLog> {

    /**
     * 处理操作日志
     * @param operLogEvent 操作日志事件
     */
    public void handleOperLog(OperLogEvent operLogEvent);
}
