package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.model.dto.operlog.SysOperLogQueryDTO;
import com.whim.system.model.entity.SysOperLog;
import com.whim.system.model.vo.operlog.SysOperLogDetailVO;
import com.whim.system.model.vo.operlog.SysOperLogVO;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 系统操作日志管理服务。
 */
public interface ISysOperLogService extends IService<SysOperLog> {
    /**
     * 按筛选条件分页查询操作日志。
     *
     * @param query 操作日志筛选及分页条件
     * @return 操作日志分页数据
     */
    PageDataVO<SysOperLogVO> getOperLogPage(SysOperLogQueryDTO query);

    /**
     * 根据主键查询操作日志详情。
     *
     * @param id 操作日志主键
     * @return 操作日志详情
     */
    SysOperLogDetailVO getOperLogDetail(Long id);

    /**
     * 逻辑删除所选操作日志。
     *
     * @param ids 操作日志主键集合
     */
    void deleteOperLogs(List<Long> ids);

    /**
     * 清空本次操作可见的全部操作日志。
     */
    void clearOperLogs();

    /**
     * 按筛选条件分批导出操作日志。
     *
     * @param query 操作日志筛选条件
     * @param response 下载响应
     */
    void exportOperLogs(SysOperLogQueryDTO query, HttpServletResponse response);
}
