package com.whim.redis.config;

import com.whim.redis.config.properties.RedissonProperties;
import com.whim.redis.handler.KeyPrefixHandler;
import com.whim.redis.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJackson3Codec;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Jince
 * @date 2026/03/30
 * @description Redisson 自动配置，负责定制序列化编解码、连接参数、Key 前缀和虚拟线程适配。
 */
@AutoConfiguration(
        before = org.redisson.spring.starter.RedissonAutoConfiguration.class
)
@EnableConfigurationProperties(RedissonProperties.class)
@RequiredArgsConstructor
public class RedissonAutoConfiguration {

    private final RedissonProperties redissonProperties;
    private final Environment environment;
    private final JsonMapper jsonMapper;

    /**
     * 定制 Redisson 自动配置，统一设置编解码策略、Key 前缀和线程参数。
     *
     * @return Redisson 自动配置定制器
     */
    @Bean
    public RedissonAutoConfigurationCustomizer redissonAutoConfigurationCustomizer() {
        return config -> {
            validateTopologyConfiguration();
            config.setUseScriptCache(true);
            config.setCodec(createJsonCodec());
            config.setNameMapper(new KeyPrefixHandler(redissonProperties.getKeyPrefix()));

            if (redissonProperties.getThreads() != null) {
                config.setThreads(redissonProperties.getThreads());
            }
            if (redissonProperties.getNettyThreads() != null) {
                config.setNettyThreads(redissonProperties.getNettyThreads());
            }

            boolean virtualEnabled = Boolean.TRUE.equals(
                    environment.getProperty("spring.threads.virtual.enabled", Boolean.class));
            if (virtualEnabled) {
                config.setNettyExecutor(new VirtualThreadTaskExecutor("redisson-"));
            }

            configureSingleServer(config);
            configureClusterServers(config);
        };
    }

    /**
     * 配置单机模式
     *
     * @param config Redisson 配置对象
     */
    private void configureSingleServer(Config config) {
        RedissonProperties.SingleServerConfig ssc = redissonProperties.getSingleServerConfig();
        if (ssc == null) {
            return;
        }
        SingleServerConfig singleServerConfig = config.useSingleServer();
        applyClientName(singleServerConfig, ssc.getClientName());
        applyIfPresent(ssc.getTimeout(), singleServerConfig::setTimeout);
        applyIfPresent(ssc.getIdleConnectionTimeout(), singleServerConfig::setIdleConnectionTimeout);
        applyIfPresent(ssc.getSubscriptionConnectionPoolSize(), singleServerConfig::setSubscriptionConnectionPoolSize);
        applyIfPresent(ssc.getConnectionMinimumIdleSize(), singleServerConfig::setConnectionMinimumIdleSize);
        applyIfPresent(ssc.getConnectionPoolSize(), singleServerConfig::setConnectionPoolSize);
    }

    /**
     * 配置集群模式
     *
     * @param config Redisson 配置对象
     */
    private void configureClusterServers(Config config) {
        RedissonProperties.ClusterServersConfig csc = redissonProperties.getClusterServersConfig();
        if (csc == null) {
            return;
        }
        ClusterServersConfig clusterServersConfig = config.useClusterServers();
        applyClientName(clusterServersConfig, csc.getClientName());
        applyIfPresent(csc.getTimeout(), clusterServersConfig::setTimeout);
        applyIfPresent(csc.getIdleConnectionTimeout(), clusterServersConfig::setIdleConnectionTimeout);
        applyIfPresent(csc.getSubscriptionConnectionPoolSize(), clusterServersConfig::setSubscriptionConnectionPoolSize);
        applyIfPresent(csc.getMasterConnectionMinimumIdleSize(), clusterServersConfig::setMasterConnectionMinimumIdleSize);
        applyIfPresent(csc.getMasterConnectionPoolSize(), clusterServersConfig::setMasterConnectionPoolSize);
        applyIfPresent(csc.getSlaveConnectionMinimumIdleSize(), clusterServersConfig::setSlaveConnectionMinimumIdleSize);
        applyIfPresent(csc.getSlaveConnectionPoolSize(), clusterServersConfig::setSlaveConnectionPoolSize);
        if (csc.getReadMode() != null) {
            clusterServersConfig.setReadMode(csc.getReadMode());
        }
        if (csc.getSubscriptionMode() != null) {
            clusterServersConfig.setSubscriptionMode(csc.getSubscriptionMode());
        }
    }

    /**
     * 初始化 RedisUtils 使用的 RedissonClient。
     *
     * @param redissonClient Redisson 客户端
     * @return RedisUtils 初始化器
     */
    @Bean
    public RedisUtils.Initializer redisUtilsInitializer(RedissonClient redissonClient) {
        return new RedisUtils.Initializer(redissonClient);
    }

    /**
     * 创建 Redisson 使用的 Jackson 3 编解码器，复用 Spring Boot 统一管理的 JsonMapper。
     *
     * @return Redisson JSON 编解码器
     */
    private JsonJackson3Codec createJsonCodec() {
        return new JsonJackson3Codec(jsonMapper);
    }

    /**
     * 校验 Redisson 拓扑配置，避免单机与集群配置同时生效造成行为不确定。
     */
    private void validateTopologyConfiguration() {
        if (redissonProperties.getSingleServerConfig() != null
                && redissonProperties.getClusterServersConfig() != null) {
            throw new IllegalStateException("redisson.single-server-config 与 redisson.cluster-servers-config 不能同时配置");
        }
    }

    /**
     * 仅在客户端名称有值时写入 Redisson 配置。
     *
     * @param serverConfig Redisson 服务端配置
     * @param clientName   客户端名称
     */
    private void applyClientName(SingleServerConfig serverConfig, String clientName) {
        if (StringUtils.hasText(clientName)) {
            serverConfig.setClientName(clientName);
        }
    }

    /**
     * 仅在客户端名称有值时写入 Redisson 配置。
     *
     * @param serverConfig Redisson 服务端配置
     * @param clientName   客户端名称
     */
    private void applyClientName(ClusterServersConfig serverConfig, String clientName) {
        if (StringUtils.hasText(clientName)) {
            serverConfig.setClientName(clientName);
        }
    }

    /**
     * 仅在参数存在时应用整数配置，避免覆盖 Redisson 默认值。
     *
     * @param value    配置值
     * @param consumer 配置应用逻辑
     */
    private void applyIfPresent(Integer value, java.util.function.IntConsumer consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }
}
