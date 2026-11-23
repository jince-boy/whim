package com.whim.sse.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 系统事件流的心跳、连接限额与集群投递配置。
 */
@Getter
@Setter
@Validated
@ConfigurationProperties("whim.sse")
public class SseProperties {

    /** 是否启用 SSE 模块和订阅入口。 */
    private boolean enabled = true;

    /** 多实例部署时通过同一 Redis 主题投递。 */
    private boolean clusterEnabled;

    /** 同一集群使用的 Redis 频道，独立环境应配置不同名称。 */
    @NotBlank
    private String clusterTopic = "whim:sse:system:events";

    /** 空闲连接发送心跳的间隔，单位为秒。 */
    @Min(1)
    private int heartbeatSeconds = 25;

    /** 同一用户在单个应用实例上的最大并发订阅数。 */
    @Min(1)
    private int maxConnectionsPerUser = 12;

    /** 单个连接等待写出的事件上限。 */
    @Min(1)
    private int queueCapacity = 64;
}
