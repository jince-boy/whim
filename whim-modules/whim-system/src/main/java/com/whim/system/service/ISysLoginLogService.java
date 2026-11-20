package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.mybatisplus.model.vo.PageDataVO;
import com.whim.system.model.dto.loginlog.SysLoginLogQueryDTO;
import com.whim.system.model.entity.SysLoginLog;
import com.whim.system.model.vo.loginlog.SysLoginLogVO;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Jince
 * @date 2026/11/19
 * @description 系统登录日志管理服务。
 */
public interface ISysLoginLogService extends IService<SysLoginLog> {
    /**
     * 按筛选条件分页查询登录日志。
     *
     * @param query 登录日志筛选及分页条件
     * @return 登录日志分页数据
     */
    PageDataVO<SysLoginLogVO> getLoginLogPage(SysLoginLogQueryDTO query);

    /**
     * 清空本次操作可见的全部登录日志。
     */
    void clearLoginLogs();

    /**
     * 按筛选条件分批导出登录日志。
     *
     * @param query 登录日志筛选条件
     * @param response 下载响应
     */
    void exportLoginLogs(SysLoginLogQueryDTO query, HttpServletResponse response);
}
