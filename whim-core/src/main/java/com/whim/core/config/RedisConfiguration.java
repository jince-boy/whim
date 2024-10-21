package com.whim.core.config;

import com.whim.common.serializer.JacksonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author Jince
 * date: 2024/10/20 02:02
 * description: Redis配置文件
 */
@Configuration
public class RedisConfiguration {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        //设置Redis连接工厂
        template.setConnectionFactory(factory);
        JacksonRedisSerializer<Object> objectJacksonRedisSerializer = new JacksonRedisSerializer<>(Object.class);
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        // 使用自定义序列化和反序列化redis的value值
        template.setValueSerializer(objectJacksonRedisSerializer);
        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        // 使用自定义序列化和反序列化redis的value值
        template.setHashValueSerializer(objectJacksonRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
