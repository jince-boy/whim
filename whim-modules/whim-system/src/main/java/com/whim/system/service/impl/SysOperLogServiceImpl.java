package com.whim.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whim.core.auth.AuthenticationContext;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.BeanConvertUtils;
import com.whim.excel.utils.ExcelBuilder;
import com.whim.log.enums.LogStatus;
import com.whim.log.enums.LogType;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.mapper.SysOperLogMapper;
import com.whim.system.model.dto.operlog.SysOperLogQueryDTO;
import com.whim.system.model.entity.SysOperLog;
import com.whim.system.model.vo.dict.SysDictDataVO;
import com.whim.system.model.vo.operlog.SysOperLogDetailVO;
import com.whim.system.model.vo.operlog.SysOperLogVO;
import com.whim.system.service.ISysDictDataService;
import com.whim.system.service.ISysOperLogService;
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
 * @description 系统级操作日志查询与维护。
 */
@Service
@RequiredArgsConstructor
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements ISysOperLogService {
    private static final int BATCH_SIZE = 500;

    private final ISysDictDataService dictDataService;
    private final AuthenticationContext authenticationContext;

    /**
     * 按筛选条件分页查询操作日志。
     *
     * @param query 操作日志筛选及分页条件
     * @return 操作日志分页数据
     */
    @Override
    public PageDataVO<SysOperLogVO> getOperLogPage(SysOperLogQueryDTO query) {
        return new PageDataVO<>(page(query.getPage(), getOperLogQuery(query))
                .convert(row -> BeanConvertUtils.convert(row, SysOperLogVO.class)));
    }

    /**
     * 根据主键查询操作日志详情。
     *
     * @param id 操作日志主键
     * @return 操作日志详情
     */
    @Override
    public SysOperLogDetailVO getOperLogDetail(Long id) {
        SysOperLog entry = getById(id);
        if (entry == null) {
            throw new ServiceException("操作日志不存在");
        }
        return BeanConvertUtils.convert(entry, SysOperLogDetailVO.class);
    }

    /**
     * 逻辑删除所选操作日志。
     *
     * @param ids 操作日志主键集合
     */
    @Override
    public void deleteOperLogs(List<Long> ids) {
        removeByIds(ids);
    }

    /**
     * 清空本次操作可见的全部操作日志。
     */
    @Override
    public void clearOperLogs() {
        // 以当前最大主键为清空边界，后续生成的更大主键留给下一次操作。
        List<SysOperLog> latest = lambdaQuery().select(SysOperLog::getId)
                .orderByDesc(SysOperLog::getId).page(new Page<>(1, 1, false)).getRecords();
        if (!latest.isEmpty()) {
            baseMapper.clearOperLogs(latest.getFirst().getId(), authenticationContext.getUserId());
        }
    }

    /**
     * 按筛选条件分批导出操作日志。
     *
     * @param query 操作日志筛选条件
     * @param response 下载响应
     */
    @Override
    public void exportOperLogs(SysOperLogQueryDTO query, HttpServletResponse response) {
        Map<String, String> labels = dictDataService.getDictDataByType(LogStatus.DICTIONARY_TYPE).stream()
                .collect(Collectors.toMap(SysDictDataVO::getValue, SysDictDataVO::getLabel));
        Map<String, String> types = dictDataService.getDictDataByType(LogType.DICTIONARY_TYPE).stream()
                .collect(Collectors.toMap(SysDictDataVO::getValue, SysDictDataVO::getLabel));
        // 时间相同时以主键继续翻页，导出过程中新增的较新记录不会推移已读取的页。
        ExcelBuilder.writer(SysOperLogVO.class).sheetName("操作日志").filename("操作日志.xlsx")
                .cursor(lastRow -> {
                    LambdaQueryWrapper<SysOperLog> wrapper = getOperLogQuery(query);
                    if (lastRow != null) {
                        wrapper.and(cursor -> cursor.lt(SysOperLog::getOperTime, lastRow.getOperTime())
                                .or(sameTime -> sameTime.eq(SysOperLog::getOperTime, lastRow.getOperTime())
                                        .lt(SysOperLog::getId, lastRow.getId())));
                    }
                    List<SysOperLog> rows = page(new Page<>(1, BATCH_SIZE, false), wrapper).getRecords();
                    List<SysOperLogVO> result = BeanConvertUtils.convertList(rows, SysOperLogVO.class);
                    for (SysOperLogVO row : result) {
                        String value = row.getStatus().toString();
                        String type = row.getLogType().toString();
                        row.setTypeLabel(types.getOrDefault(type, type));
                        row.setStatusLabel(labels.getOrDefault(value, value));
                    }
                    return result;
                }).toResponse(response);
    }

    /**
     * 创建列表与导出共用的筛选及排序条件。
     *
     * @param query 操作日志筛选条件
     * @return 按操作时间和主键倒序的查询条件
     */
    private LambdaQueryWrapper<SysOperLog> getOperLogQuery(SysOperLogQueryDTO query) {
        return new LambdaQueryWrapper<SysOperLog>()
                .select(SysOperLog::getId, SysOperLog::getTitle, SysOperLog::getOperName, SysOperLog::getLogType,
                        SysOperLog::getOperIp, SysOperLog::getOperLocation, SysOperLog::getOperTime,
                        SysOperLog::getStatus, SysOperLog::getCostTime)
                .eq(StringUtils.isNotBlank(query.getTitle()), SysOperLog::getTitle, query.getTitle())
                .eq(StringUtils.isNotBlank(query.getOperName()), SysOperLog::getOperName, query.getOperName())
                .eq(query.getLogType() != null, SysOperLog::getLogType, query.getLogType())
                .eq(StringUtils.isNotBlank(query.getIpAddress()), SysOperLog::getOperIp, query.getIpAddress())
                .eq(query.getStatus() != null, SysOperLog::getStatus, query.getStatus())
                .ge(query.getStartTime() != null, SysOperLog::getOperTime, query.getStartTime())
                .le(query.getEndTime() != null, SysOperLog::getOperTime, query.getEndTime())
                .orderByDesc(SysOperLog::getOperTime, SysOperLog::getId);
    }
}
