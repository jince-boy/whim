package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.satoken.event.LoginEvent;
import com.whim.system.model.entity.SysLoginLog;

/**
 * 系统登录日志(SysLoginLog)表服务接口
 *
 * @author Jince
 * @since 2025-08-17 17:15:29
 */
public interface ISysLoginLogService extends IService<SysLoginLog> {
    /**
     * 处理登录日志
     *
     * @param loginEvent 登录事件
     */
    void handleLoginLog(LoginEvent loginEvent);
}
