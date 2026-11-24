package com.whim.system.service;

import com.whim.system.model.vo.servermonitor.ServerMonitorSnapshotVO;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 服务器监控服务。
 */
public interface ISysServerMonitorService {
    /**
     * 查询当前应用实例可见的服务器资源。
     *
     * @return 服务器监控快照
     */
    ServerMonitorSnapshotVO getServerMonitorSnapshot();
}
