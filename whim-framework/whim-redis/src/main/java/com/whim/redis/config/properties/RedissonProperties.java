package com.whim.redis.config.properties;

import lombok.Data;
import org.redisson.config.ReadMode;
import org.redisson.config.SubscriptionMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Jince
 * @date 2026/03/30
 * @description Redisson 配置属性
 */
@Data
@ConfigurationProperties(prefix = "redisson")
public class RedissonProperties {

    /**
     * Redis 缓存 key 全局前缀，用于隔离不同应用或环境的数据。
     */
    private String keyPrefix;

    /**
     * 线程池数量。null 表示使用 Redisson 默认值（处理核心数 × 2）。
     */
    private Integer threads;

    /**
     * Netty 线程池数量。null 表示使用 Redisson 默认值（处理核心数 × 2）。
     */
    private Integer nettyThreads;

    /**
     * 单机服务配置，与 clusterServersConfig 互斥。
     */
    private SingleServerConfig singleServerConfig;

    /**
     * 集群服务配置，与 singleServerConfig 互斥。
     */
    private ClusterServersConfig clusterServersConfig;

    /**
     * @author Jince
     * @date 2026/03/30
     * @description 单机模式配置
     */
    @Data
    public static class SingleServerConfig {

        /**
         * 客户端名称
         */
        private String clientName;

        /**
         * 最小空闲连接数
         */
        private int connectionMinimumIdleSize;

        /**
         * 连接池大小
         */
        private int connectionPoolSize;

        /**
         * 连接空闲超时（毫秒）
         */
        private int idleConnectionTimeout;

        /**
         * 命令等待超时（毫秒）
         */
        private int timeout;

        /**
         * 发布/订阅连接池大小
         */
        private int subscriptionConnectionPoolSize;
    }

    /**
     * @author Jince
     * @date 2026/03/30
     * @description 集群模式配置
     */
    @Data
    public static class ClusterServersConfig {

        /**
         * 客户端名称
         */
        private String clientName;

        /**
         * master 最小空闲连接数
         */
        private int masterConnectionMinimumIdleSize;

        /**
         * master 连接池大小
         */
        private int masterConnectionPoolSize;

        /**
         * slave 最小空闲连接数
         */
        private int slaveConnectionMinimumIdleSize;

        /**
         * slave 连接池大小
         */
        private int slaveConnectionPoolSize;

        /**
         * 连接空闲超时（毫秒）
         */
        private int idleConnectionTimeout;

        /**
         * 命令等待超时（毫秒）
         */
        private int timeout;

        /**
         * 发布/订阅连接池大小
         */
        private int subscriptionConnectionPoolSize;

        /**
         * 读取模式
         */
        private ReadMode readMode;

        /**
         * 订阅模式
         */
        private SubscriptionMode subscriptionMode;
    }
}
