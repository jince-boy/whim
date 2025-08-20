package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.utils.ConvertUtils;
import com.whim.log.event.OperLogEvent;
import com.whim.mybatis.core.model.dto.PageQueryDTO;
import com.whim.mybatis.core.model.vo.PageDataVO;
import com.whim.system.mapper.SysOperLogMapper;
import com.whim.system.model.dto.sysOperLog.SysOperLogQueryDTO;
import com.whim.system.model.entity.SysOperLog;
import com.whim.system.service.ISysOperLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 操作日志(SysOperLog)表服务实现类
 *
 * @author Jince
 * @since 2025-08-19 21:46:45
 */
@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements ISysOperLogService {

    /**
     * 分页查询操作日志
     *
     * @param sysOperLogQueryDTO 查询参数
     * @param pageQueryDTO       分页参数
     * @return 分页数据
     */
    @Override
    public PageDataVO<SysOperLog> getOperLogPage(SysOperLogQueryDTO sysOperLogQueryDTO, PageQueryDTO pageQueryDTO) {
        LambdaQueryWrapper<SysOperLog> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .like(StringUtils.isNotBlank(sysOperLogQueryDTO.getTitle()), SysOperLog::getTitle, sysOperLogQueryDTO.getTitle())
                .like(StringUtils.isNotBlank(sysOperLogQueryDTO.getOperName()), SysOperLog::getOperName, sysOperLogQueryDTO.getOperName())
                .like(StringUtils.isNotBlank(sysOperLogQueryDTO.getOperIp()), SysOperLog::getOperIp, sysOperLogQueryDTO.getOperIp())
                .eq(Objects.nonNull(sysOperLogQueryDTO.getStatus()), SysOperLog::getStatus, sysOperLogQueryDTO.getStatus())
                .eq(Objects.nonNull(sysOperLogQueryDTO.getLogType()), SysOperLog::getLogType, sysOperLogQueryDTO.getLogType())
                .between(sysOperLogQueryDTO.getStartTime() != null && sysOperLogQueryDTO.getEndTime() != null, SysOperLog::getOperTime, sysOperLogQueryDTO.getStartTime(), sysOperLogQueryDTO.getEndTime());
        return new PageDataVO<>(this.page(pageQueryDTO.getPage(), lambdaQueryWrapper));
    }

    /**
     * 删除操作日志
     *
     * @param operLogIds 操作日志ID
     */
    @Override
    public void deleteOperLogByIds(Long[] operLogIds) {
        this.removeByIds(Arrays.asList(operLogIds));
    }

    /**
     * 清空操作日志
     */
    @Override
    public void cleanOperLog() {
        this.remove(new QueryWrapper<SysOperLog>().eq("1", "1"));
    }

    /**
     * 获取操作日志列表
     *
     * @return 操作日志列表
     */
    @Override
    public List<SysOperLog> getOperLogList() {
        return this.list();
    }

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

