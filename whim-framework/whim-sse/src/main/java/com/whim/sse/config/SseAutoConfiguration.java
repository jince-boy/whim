package com.whim.sse.config;

import com.whim.sse.SseConnectionRegistry;
import com.whim.sse.SseEventPublisher;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 为 Servlet 应用装配本地连接管理与可选的 Redis 事件分发。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnBooleanProperty(prefix = "whim.sse", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(SseProperties.class)
public class SseAutoConfiguration {

    /**
     * 创建当前实例的连接注册表。
     *
     * @param properties 事件流配置
     * @return 本地连接注册表
     */
    @Bean
    public SseConnectionRegistry sseConnectionRegistry(SseProperties properties) {
        return new SseConnectionRegistry(properties);
    }

    /**
     * 创建事件发布器，集群模式要求应用已配置 Redis。
     *
     * @param registry 当前实例的连接注册表
     * @param properties 事件流配置
     * @param clients 应用中的 Redis 客户端
     * @param jsonMapper 应用统一的 JSON 映射器
     * @return 系统事件发布器
     */
    @Bean
    public SseEventPublisher sseEventPublisher(SseConnectionRegistry registry, SseProperties properties,
                                               ObjectProvider<RedissonClient> clients, JsonMapper jsonMapper) {
        RTopic topic = properties.isClusterEnabled()
                ? clients.getObject().getTopic(properties.getClusterTopic(), StringCodec.INSTANCE) : null;
        return new SseEventPublisher(registry, jsonMapper, topic);
    }
}
