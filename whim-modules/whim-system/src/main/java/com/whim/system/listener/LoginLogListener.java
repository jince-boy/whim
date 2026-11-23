package com.whim.system.listener;

import com.whim.core.exception.ServiceException;
import com.whim.core.utils.IPUtils;
import com.whim.core.utils.ServletUtils;
import com.whim.log.config.LogAutoConfiguration;
import com.whim.log.event.LoginLogEvent;
import com.whim.redis.utils.RedisUtils;
import com.whim.system.model.entity.SysLoginLog;
import com.whim.system.service.ISysLoginLogService;
import com.whim.system.service.ISysNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RateType;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 异步保存系统登录日志，并向多次凭据校验失败的账号发送安全提醒。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginLogListener {
    private final ISysLoginLogService loginLogService;
    private final ISysNoticeService noticeService;

    /**
     * 根据认证现场信息记录登录结果，合并同一账号短时间内的凭据错误提醒。
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

        Long userId = event.getCredentialFailureUserId();
        if (userId == null) {
            return;
        }
        try {
            // 同一账号跨地址累计密码与第二因素错误；达到阈值后另设冷却，持续重试不会刷屏。
            if (RedisUtils.rateLimiter("notice:loginFailures:" + userId, RateType.OVERALL, 4, 600, 1200) < 0
                    && RedisUtils.setObjectIfAbsent("notice:loginFailureAlert:" + userId, true, Duration.ofMinutes(10))) {
                noticeService.addSystemMessages(Set.of(userId), "账号多次登录失败",
                        "你的账号在短时间内多次登录失败。如果并非本人操作，请核对消息中心的登录成功提醒并修改密码。");
            }
        } catch (RuntimeException exception) {
            log.error("登录失败提醒处理失败，用户ID={}", userId, exception);
        }
    }
}
