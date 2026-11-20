package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.excel.utils.ExcelBuilder;
import com.whim.log.enums.LogStatus;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.mapper.SysLoginLogMapper;
import com.whim.system.model.dto.loginlog.SysLoginLogQueryDTO;
import com.whim.system.model.entity.SysLoginLog;
import com.whim.system.model.vo.dict.SysDictDataVO;
import com.whim.system.model.vo.loginlog.SysLoginLogVO;
import com.whim.system.service.ISysDictDataService;
import com.whim.system.service.ISysLoginLogService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 系统级登录日志查询与维护。
 */
@Service
@RequiredArgsConstructor
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements ISysLoginLogService {
    private static final int BATCH_SIZE = 500;

    private final ISysDictDataService dictDataService;
    private final AuthenticationContext authenticationContext;

    /**
     * 按筛选条件分页查询登录日志。
     *
     * @param query 登录日志筛选及分页条件
     * @return 登录日志分页数据
     */
    @Override
    public PageDataVO<SysLoginLogVO> getLoginLogPage(SysLoginLogQueryDTO query) {
        return new PageDataVO<>(page(query.getPage(), getLoginLogQuery(query))
                .convert(row -> BeanConvertUtils.convert(row, SysLoginLogVO.class)));
    }

    /**
     * 清空本次操作可见的全部登录日志。
     */
    @Override
    public void clearLoginLogs() {
        // 以当前最大主键为清空边界，后续生成的更大主键留给下一次操作。
        List<SysLoginLog> latest = lambdaQuery().select(SysLoginLog::getId)
                .orderByDesc(SysLoginLog::getId).page(new Page<>(1, 1, false)).getRecords();
        if (!latest.isEmpty()) {
            baseMapper.clearLoginLogs(latest.getFirst().getId(), authenticationContext.getUserId());
        }
    }

    /**
     * 按筛选条件分批导出登录日志。
     *
     * @param query 登录日志筛选条件
     * @param response 下载响应
     */
    @Override
    public void exportLoginLogs(SysLoginLogQueryDTO query, HttpServletResponse response) {
        Map<String, String> labels = dictDataService.getDictDataByType(LogStatus.DICTIONARY_TYPE).stream()
                .collect(Collectors.toMap(SysDictDataVO::getValue, SysDictDataVO::getLabel));
        // 时间相同时以主键继续翻页，导出过程中新增的较新记录不会推移已读取的页。
        ExcelBuilder.writer(SysLoginLogVO.class).sheetName("登录日志").filename("登录日志.xlsx")
                .cursor(lastRow -> {
                    LambdaQueryWrapper<SysLoginLog> wrapper = getLoginLogQuery(query);
                    if (lastRow != null) {
                        wrapper.and(cursor -> cursor.lt(SysLoginLog::getLoginTime, lastRow.getLoginTime())
                                .or(sameTime -> sameTime.eq(SysLoginLog::getLoginTime, lastRow.getLoginTime())
                                        .lt(SysLoginLog::getId, lastRow.getId())));
                    }
                    List<SysLoginLog> rows = page(new Page<>(1, BATCH_SIZE, false), wrapper).getRecords();
                    List<SysLoginLogVO> result = BeanConvertUtils.convertList(rows, SysLoginLogVO.class);
                    for (SysLoginLogVO row : result) {
                        String value = row.getStatus().toString();
                        row.setStatusLabel(labels.getOrDefault(value, value));
                    }
                    return result;
                }).toResponse(response);
    }

    /**
     * 创建列表与导出共用的筛选及排序条件。
     *
     * @param query 登录日志筛选条件
     * @return 按登录时间和主键倒序的查询条件
     */
    private LambdaQueryWrapper<SysLoginLog> getLoginLogQuery(SysLoginLogQueryDTO query) {
        return new LambdaQueryWrapper<SysLoginLog>()
                .select(SysLoginLog::getId, SysLoginLog::getUsername, SysLoginLog::getIpAddress,
                        SysLoginLog::getLoginLocation, SysLoginLog::getBrowser, SysLoginLog::getOs,
                        SysLoginLog::getDeviceType, SysLoginLog::getStatus, SysLoginLog::getRemark,
                        SysLoginLog::getLoginTime)
                .eq(StringUtils.isNotBlank(query.getUsername()), SysLoginLog::getUsername, query.getUsername())
                .eq(StringUtils.isNotBlank(query.getIpAddress()), SysLoginLog::getIpAddress, query.getIpAddress())
                .eq(query.getStatus() != null, SysLoginLog::getStatus, query.getStatus())
                .ge(query.getStartTime() != null, SysLoginLog::getLoginTime, query.getStartTime())
                .le(query.getEndTime() != null, SysLoginLog::getLoginTime, query.getEndTime())
                .orderByDesc(SysLoginLog::getLoginTime, SysLoginLog::getId);
    }
}
