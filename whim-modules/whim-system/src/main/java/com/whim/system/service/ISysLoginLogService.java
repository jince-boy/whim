package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.mybatis.core.model.dto.PageQueryDTO;
import com.whim.mybatis.core.model.vo.PageDataVO;
import com.whim.satoken.event.LoginEvent;
import com.whim.system.model.dto.sysLoginLog.SysLoginLogQueryDTO;
import com.whim.system.model.entity.SysLoginLog;

import java.util.List;

/**
 * 系统登录日志(SysLoginLog)表服务接口
 *
 * @author Jince
 * @since 2025-08-17 17:15:29
 */
public interface ISysLoginLogService extends IService<SysLoginLog> {

    /**
     * 获取登录日志分页列表
     *
     * @param sysLoginLogQueryDTO 查询参数
     * @param pageQueryDTO        分页参数
     * @return 分页列表
     */
    PageDataVO<SysLoginLog> getLoginLogPage(SysLoginLogQueryDTO sysLoginLogQueryDTO, PageQueryDTO pageQueryDTO);

    /**
     * 删除登录日志
     *
     * @param loginLogIds 登录日志ID集合
     */
    void deleteLoginLogByIds(Long[] loginLogIds);

    /**
     * 删除所有登录日志
     */
    void deleteLoginLogAll();

    /**
     * 获取登录日志列表
     * @return 登录日志列表
     */
    List<SysLoginLog> getLoginLogList();

    /**
     * 处理登录日志
     *
     * @param loginEvent 登录事件
     */
    void handleLoginLog(LoginEvent loginEvent);
}
