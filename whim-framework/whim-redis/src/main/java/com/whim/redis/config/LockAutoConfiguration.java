package com.whim.redis.config;

import com.baomidou.lock.LockFailureStrategy;
import com.whim.core.exception.LockException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author Jince
 * @date 2026/03/31
 * @description Lock4j 自动配置。
 * 提供默认的锁获取失败策略：将 Lock4j 内部异常转换为 {@link LockException}，
 * 使上层模块无需直接依赖 Lock4j 即可统一处理锁异常。
 */
@AutoConfiguration
public class LockAutoConfiguration {

    /**
     * 默认锁获取失败策略，将失败转换为 {@link LockException}。
     * 应用可通过自定义 {@link LockFailureStrategy} Bean 覆盖此行为。
     *
     * @return 锁获取失败策略
     */
    @Bean
    @ConditionalOnMissingBean
    public LockFailureStrategy lockFailureStrategy() {
        return (key, method, arguments) -> {
            throw new LockException("获取分布式锁失败");
        };
    }
}
