package com.whim.system.listener;

import com.whim.core.exception.ServiceException;
import com.whim.core.utils.IPUtils;
import com.whim.log.config.LogAutoConfiguration;
import com.whim.log.event.OperLogEvent;
import com.whim.system.model.entity.SysOperLog;
import com.whim.system.service.ISysOperLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 异步写入控制器操作审计事件。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OperLogListener {
    private final ISysOperLogService operLogService;

    /**
     * 保存事件快照并解析操作地点。
     *
     * @param event 控制器操作事件
     */
    @Async(LogAutoConfiguration.AUDIT_EXECUTOR)
    @EventListener
    public void addOperLog(OperLogEvent event) {
        try {
            SysOperLog entry = new SysOperLog();
            entry.setTitle(event.getTitle());
            entry.setLogType(event.getLogType().getCode());
            entry.setStatus(event.getStatus().getCode());
            entry.setMethodName(event.getMethodName());
            entry.setOperName(event.getOperName());
            entry.setOperIp(event.getOperIp());
            entry.setRequestMethod(event.getRequestMethod());
            entry.setRequestUrl(event.getRequestUrl());
            entry.setRequestParam(event.getRequestParam());
            entry.setResponseParam(event.getResponseParam());
            entry.setErrorMessage(event.getErrorMessage());
            entry.setOperTime(event.getOperTime());
            entry.setCostTime(event.getCostTime());
            // 离线库异常只影响地点，仍保留业务操作结果。
            try {
                entry.setOperLocation(StringUtils.left(IPUtils.getCityInfo(event.getOperIp()), 255));
            } catch (ServiceException exception) {
                log.warn("操作日志缺少IP归属地，客户端地址={}", event.getOperIp());
            }
            if (!operLogService.save(entry)) {
                throw new ServiceException("操作日志保存失败");
            }
        } catch (RuntimeException exception) {
            log.error("操作日志写入失败，模块={}，操作时间={}", event.getTitle(), event.getOperTime(), exception);
        }
    }
}
