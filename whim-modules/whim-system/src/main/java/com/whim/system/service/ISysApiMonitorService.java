package com.whim.system.service;

import com.whim.system.model.vo.apimonitor.ApiMonitorSnapshotVO;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 接口监控服务。
 */
public interface ISysApiMonitorService {
    /**
     * 获取当前实例最近五分钟内截至本次查询已完成请求的统计。
     *
     * @return 请求趋势和按平均耗时降序排列的接口明细
     */
    ApiMonitorSnapshotVO getApiMonitorSnapshot();
}
