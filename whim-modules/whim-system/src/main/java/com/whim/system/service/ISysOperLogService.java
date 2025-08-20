package com.whim.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whim.log.event.OperLogEvent;
import com.whim.mybatis.core.model.dto.PageQueryDTO;
import com.whim.mybatis.core.model.vo.PageDataVO;
import com.whim.system.model.dto.sysOperLog.SysOperLogQueryDTO;
import com.whim.system.model.entity.SysOperLog;

import java.util.List;

/**
 * 操作日志(SysOperLog)表服务接口
 *
 * @author Jince
 * @since 2025-08-19 21:46:44
 */
public interface ISysOperLogService extends IService<SysOperLog> {

    /**
     * 分页查询操作日志
     *
     * @param sysOperLogQueryDTO 查询参数
     * @param pageQueryDTO       分页参数
     * @return 分页数据
     */
    PageDataVO<SysOperLog> getOperLogPage(SysOperLogQueryDTO sysOperLogQueryDTO, PageQueryDTO pageQueryDTO);

    /**
     * 删除操作日志
     *
     * @param operLogIds 操作日志ID
     */
    void deleteOperLogByIds(Long[] operLogIds);

    /**
     * 清空操作日志
     */
    void cleanOperLog();

    /**
     * 获取操作日志列表
     *
     * @return 操作日志列表
     */

    List<SysOperLog> getOperLogList();

    /**
     * 处理操作日志
     *
     * @param operLogEvent 操作日志事件
     */
    public void handleOperLog(OperLogEvent operLogEvent);
}
