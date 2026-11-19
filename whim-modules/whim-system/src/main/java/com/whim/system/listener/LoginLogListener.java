package com.whim.system.listener;

import com.whim.core.exception.ServiceException;
import com.whim.core.utils.IPUtils;
import com.whim.core.utils.ServletUtils;
import com.whim.log.config.LogAutoConfiguration;
import com.whim.log.event.LoginLogEvent;
import com.whim.system.model.entity.SysLoginLog;
import com.whim.system.service.ISysLoginLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 异步解析并保存系统登录日志。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginLogListener {
    private final ISysLoginLogService loginLogService;

    /**
     * 在审计线程中根据认证现场采集的信息写入登录日志。
     *
     * @param event 登录结果事件
     */
    @Async(LogAutoConfiguration.AUDIT_EXECUTOR)
    @EventListener
    public void addLoginLog(LoginLogEvent event) {
        try {
            SysLoginLog logEntry = new SysLoginLog();
            logEntry.setUsername(event.getUsername());
            logEntry.setIpAddress(event.getIpAddress());
            logEntry.setLoginTime(event.getLoginTime());
            logEntry.setStatus(event.getStatus().getCode());
            logEntry.setRemark(event.getRemark());
            // 离线归属地属于补充信息，数据库文件异常时仍保留认证结果。
            try {
                logEntry.setLoginLocation(StringUtils.left(IPUtils.getCityInfo(event.getIpAddress()), 255));
            } catch (ServiceException exception) {
                log.warn("登录日志缺少IP归属地，客户端地址={}", event.getIpAddress());
            }
            ServletUtils.ClientAgentInfo agent = ServletUtils.parseClientAgent(event.getUserAgent());
            logEntry.setBrowser(StringUtils.left(agent.browser(), 128));
            logEntry.setOs(StringUtils.left(agent.operatingSystem(), 128));
            logEntry.setDeviceType(StringUtils.left(agent.deviceClass(), 64));
            if (!loginLogService.save(logEntry)) {
                throw new ServiceException("登录日志保存失败");
            }
        } catch (RuntimeException exception) {
            log.error("登录日志写入失败，客户端地址={}，登录时间={}", event.getIpAddress(), event.getLoginTime(), exception);
        }
    }
}
