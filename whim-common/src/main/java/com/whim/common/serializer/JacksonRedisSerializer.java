package com.whim.common.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * @author Jince
 * date: 2024/10/20 01:21
 * description: 自定义redis value序列化
 */
public class JacksonRedisSerializer<T> implements RedisSerializer<T> {
    private final Class<T> tClass;
    private final ObjectMapper objectMapper;

    public JacksonRedisSerializer(Class<T> tClass) {
        super();
        this.tClass = tClass;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public byte[] serialize(T value) throws SerializationException {
        if (value == null) {
            return new byte[0];
        }
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (Exception e) {
            throw new SerializationException("Could not serialize object", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {

            return objectMapper.readValue(bytes, this.tClass);
        } catch (Exception e) {
            throw new SerializationException("Could not deserialize bytes", e);
        }
    }
}
