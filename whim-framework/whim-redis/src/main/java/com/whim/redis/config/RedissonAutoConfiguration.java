package com.whim.redis.config;

import com.whim.json.config.JacksonConfig;
import com.whim.json.module.BigNumberJacksonModule;
import com.whim.json.module.DateTimeJacksonModule;
import com.whim.redis.codec.RedisJsonCodec;
import com.whim.redis.config.properties.RedissonProperties;
import com.whim.redis.handler.KeyPrefixHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.task.VirtualThreadTaskExecutor;

/**
 * @author Jince
 * @date 2026/03/30
 * @description Redisson 自动配置，负责定制序列化编解码、连接参数、Key 前缀和虚拟线程适配。
 */
@Slf4j
@AutoConfiguration(
        after = JacksonConfig.class,
        before = org.redisson.spring.starter.RedissonAutoConfiguration.class
)
@EnableConfigurationProperties(RedissonProperties.class)
@RequiredArgsConstructor
public class RedissonAutoConfiguration {

    private final RedissonProperties redissonProperties;
    private final Environment environment;

    /**
     * 定制 Redisson 自动配置，复用 whim-json 的统一序列化模块。
     *
     * @param dateTimeJacksonModule  时间序列化模块
     * @param bigNumberJacksonModule 大数字序列化模块
     * @return Redisson 自动配置定制器
     */
    @Bean
    public RedissonAutoConfigurationCustomizer redissonAutoConfigurationCustomizer(
            DateTimeJacksonModule dateTimeJacksonModule,
            BigNumberJacksonModule bigNumberJacksonModule) {
        return config -> {
            RedisJsonCodec codec = new RedisJsonCodec(dateTimeJacksonModule, bigNumberJacksonModule);
            config.setCodec(codec).setUseScriptCache(true);

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
    private void configureSingleServer(org.redisson.config.Config config) {
        RedissonProperties.SingleServerConfig ssc = redissonProperties.getSingleServerConfig();
        if (ssc == null) {
            return;
        }
        config.useSingleServer()
                .setNameMapper(new KeyPrefixHandler(redissonProperties.getKeyPrefix()))
                .setTimeout(ssc.getTimeout())
                .setClientName(ssc.getClientName())
                .setIdleConnectionTimeout(ssc.getIdleConnectionTimeout())
                .setSubscriptionConnectionPoolSize(ssc.getSubscriptionConnectionPoolSize())
                .setConnectionMinimumIdleSize(ssc.getConnectionMinimumIdleSize())
                .setConnectionPoolSize(ssc.getConnectionPoolSize());
    }

    /**
     * 配置集群模式
     *
     * @param config Redisson 配置对象
     */
    private void configureClusterServers(org.redisson.config.Config config) {
        RedissonProperties.ClusterServersConfig csc = redissonProperties.getClusterServersConfig();
        if (csc == null) {
            return;
        }
        config.useClusterServers()
                .setNameMapper(new KeyPrefixHandler(redissonProperties.getKeyPrefix()))
                .setTimeout(csc.getTimeout())
                .setClientName(csc.getClientName())
                .setIdleConnectionTimeout(csc.getIdleConnectionTimeout())
                .setSubscriptionConnectionPoolSize(csc.getSubscriptionConnectionPoolSize())
                .setMasterConnectionMinimumIdleSize(csc.getMasterConnectionMinimumIdleSize())
                .setMasterConnectionPoolSize(csc.getMasterConnectionPoolSize())
                .setSlaveConnectionMinimumIdleSize(csc.getSlaveConnectionMinimumIdleSize())
                .setSlaveConnectionPoolSize(csc.getSlaveConnectionPoolSize())
                .setReadMode(csc.getReadMode())
                .setSubscriptionMode(csc.getSubscriptionMode());
    }
}
