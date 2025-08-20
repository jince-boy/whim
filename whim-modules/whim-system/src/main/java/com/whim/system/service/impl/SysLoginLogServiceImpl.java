package com.whim.system.service.impl;

import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.utils.IPUtils;
import com.whim.core.utils.ServletUtils;
import com.whim.mybatis.core.model.dto.PageQueryDTO;
import com.whim.mybatis.core.model.vo.PageDataVO;
import com.whim.satoken.event.LoginEvent;
import com.whim.system.mapper.SysLoginLogMapper;
import com.whim.system.model.dto.sysLoginLog.SysLoginLogQueryDTO;
import com.whim.system.model.entity.SysLoginLog;
import com.whim.system.service.ISysLoginLogService;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 系统登录日志(SysLoginLog)表服务实现类
 *
 * @author Jince
 * @since 2025-08-17 17:15:29
 */
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements ISysLoginLogService {

    /**
     * 获取登录日志分页列表
     *
     * @param sysLoginLogQueryDTO 查询参数
     * @param pageQueryDTO        分页参数
     * @return 分页列表
     */
    @Override
    public PageDataVO<SysLoginLog> getLoginLogPage(SysLoginLogQueryDTO sysLoginLogQueryDTO, PageQueryDTO pageQueryDTO) {
        LambdaQueryWrapper<SysLoginLog> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .like(StringUtils.isNotBlank(sysLoginLogQueryDTO.getUsername()), SysLoginLog::getUsername, sysLoginLogQueryDTO.getUsername())
                .like(StringUtils.isNotBlank(sysLoginLogQueryDTO.getIpAddress()), SysLoginLog::getIpAddress, sysLoginLogQueryDTO.getIpAddress())
                .eq(Objects.nonNull(sysLoginLogQueryDTO.getStatus()), SysLoginLog::getStatus, sysLoginLogQueryDTO.getStatus());
        lambdaQueryWrapper.between(sysLoginLogQueryDTO.getStartTime() != null && sysLoginLogQueryDTO.getEndTime() != null, SysLoginLog::getLoginTime, sysLoginLogQueryDTO.getStartTime(), sysLoginLogQueryDTO.getEndTime());
        return new PageDataVO<>(this.page(pageQueryDTO.getPage(), lambdaQueryWrapper));
    }

    /**
     * 删除登录日志
     *
     * @param loginLogIds 登录日志ID集合
     */
    @Override
    public void deleteLoginLogByIds(Long[] loginLogIds) {
        this.removeByIds(Arrays.asList(loginLogIds));
    }

    /**
     * 删除所有登录日志
     */
    @Override
    public void deleteLoginLogAll() {
        this.remove(new QueryWrapper<SysLoginLog>().eq("1", "1"));
    }


    /**
     * 获取登录日志列表
     *
     * @return 登录日志列表
     */
    @Override
    public List<SysLoginLog> getLoginLogList() {
        return this.list();
    }

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

