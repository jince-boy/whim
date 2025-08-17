package com.whim.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.utils.IPUtils;
import com.whim.core.utils.ServletUtils;
import com.whim.satoken.event.LoginEvent;
import com.whim.system.mapper.SysLoginLogMapper;
import com.whim.system.model.entity.SysLoginLog;
import com.whim.system.service.ISysLoginLogService;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 系统登录日志(SysLoginLog)表服务实现类
 *
 * @author Jince
 * @since 2025-08-17 17:15:29
 */
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements ISysLoginLogService {
    /**
     * 处理登录日志
     *
     * @param loginEvent 登录事件
     */
    @Async
    @EventListener
    @Override
    public void handleLoginLog(LoginEvent loginEvent) {
        UserAgent userAgent = ServletUtils.getUserAgent();
        SysLoginLog sysLoginLog = new SysLoginLog();
        sysLoginLog.setUsername(loginEvent.getUsername());
        sysLoginLog.setBrowser(userAgent.getBrowser().getName());
        sysLoginLog.setOs(userAgent.getOperatingSystem().getName());
        sysLoginLog.setDeviceType(userAgent.getOperatingSystem().getDeviceType().getName());
        sysLoginLog.setLoginLocation(IPUtils.getCityInfo());
        sysLoginLog.setIpAddress(IPUtils.getClientIpAddress());
        sysLoginLog.setStatus(loginEvent.getStatus());
        sysLoginLog.setRemark(loginEvent.getRemark());
        sysLoginLog.setLoginTime(LocalDateTime.now());
        this.save(sysLoginLog);
    }
}

