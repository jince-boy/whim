package com.whim.system.model.vo.databasemonitor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;

import java.util.List;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 当前应用实例的 HikariCP 连接池即时读数。
 */
@Value
public class ConnectionPoolSnapshotVO {
    long sampledAt;
    String instanceId;
    List<Pool> pools;

    /**
     * @author Jince
     * @date 2026/11/24
     * @description 按池名归属的指标，未注册或暂时不可用的读数为空。
     */
    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class Pool {
        private final String name;
        private Integer active;
        private Integer idle;
        private Integer maximum;
        private Integer pending;
        private Long timeoutCount;
    }
}
