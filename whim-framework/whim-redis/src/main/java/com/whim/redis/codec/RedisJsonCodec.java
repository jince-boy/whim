package com.whim.redis.codec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.Module;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Jince
 * @date 2026/03/30
 * @description Redis 专用 JSON 编解码器。
 * <p>
 * 基于 Jackson 3 实现，特性：
 * <ul>
 *   <li>Map 的 key 使用 {@link StringCodec}（Redis 中 key 保持可读性）</li>
 *   <li>Value 使用 Jackson 3 JSON 序列化，嵌入类型信息以支持多态反序列化</li>
 *   <li>复用 whim-serialization 提供的时间、大数字序列化模块</li>
 *   <li>修复 Long 类型被错误反序列化为 Integer 的问题</li>
 * </ul>
 */
public class RedisJsonCodec extends BaseCodec {

    private final JsonMapper jsonMapper;

    private final Encoder valueEncoder = new Encoder() {
        @Override
        public ByteBuf encode(Object in) {
            ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
            try {
                ByteBufOutputStream os = new ByteBufOutputStream(out);
                jsonMapper.writeValue((OutputStream) os, in);
                return os.buffer();
            } catch (JacksonException e) {
                out.release();
                throw e;
            }
        }
    };

    private final Decoder<Object> valueDecoder = new Decoder<>() {
        @Override
        public Object decode(ByteBuf buf, State state) {
            return jsonMapper.readValue((InputStream) new ByteBufInputStream(buf), Object.class);
        }
    };

    /**
     * 使用指定的 Jackson 模块创建编解码器，复用 whim-serialization 的统一序列化规则。
     *
     * @param modules 需要注册的 Jackson 模块（时间、大数字等）
     */
    public RedisJsonCodec(Module... modules) {
        this.jsonMapper = createJsonMapper(modules);
    }

    /**
     * 使用自定义 JsonMapper 创建编解码器
     *
     * @param jsonMapper 自定义的 JsonMapper 实例
     */
    public RedisJsonCodec(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * 获取内部使用的 JsonMapper
     *
     * @return JsonMapper 实例
     */
    public JsonMapper getJsonMapper() {
        return jsonMapper;
    }

    /**
     * 创建 Redis 专用 JsonMapper，包含类型信息、通用配置和外部序列化模块。
     *
     * @param modules 需要注册的 Jackson 模块
     * @return 配置好的 JsonMapper
     */
    private static JsonMapper createJsonMapper(Module... modules) {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class)
                .build();

        /*
         * 自定义类型解析器，基于 NON_FINAL 策略并额外处理 Long。
         * 默认 NON_FINAL 不为 final 类（如 Long）添加类型标记，
         * 会导致 Long 值在 int 范围内时被错误反序列化为 Integer。
         */
        ObjectMapper.DefaultTypeResolverBuilder typeResolver =
                new ObjectMapper.DefaultTypeResolverBuilder(ptv, ObjectMapper.DefaultTyping.NON_FINAL) {
                    @Override
                    public boolean useForType(JavaType t) {
                        if (t.getRawClass() == Long.class) {
                            return true;
                        }
                        return super.useForType(t);
                    }
                };
        typeResolver.init(JsonTypeInfo.Id.CLASS, null);
        typeResolver.inclusion(JsonTypeInfo.As.PROPERTY);

        return JsonMapper.builder()
                .findAndAddModules()
                .addModules(modules)
                .changeDefaultPropertyInclusion(incl -> incl
                        .withValueInclusion(JsonInclude.Include.NON_NULL)
                        .withContentInclusion(JsonInclude.Include.NON_NULL))
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .changeDefaultVisibility(vc -> vc
                        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE))
                .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .setDefaultTyping(typeResolver)
                .build();
    }

    @Override
    public Decoder<Object> getMapKeyDecoder() {
        return StringCodec.INSTANCE.getValueDecoder();
    }

    @Override
    public Encoder getMapKeyEncoder() {
        return StringCodec.INSTANCE.getValueEncoder();
    }

    @Override
    public Decoder<Object> getValueDecoder() {
        return valueDecoder;
    }

    @Override
    public Encoder getValueEncoder() {
        return valueEncoder;
    }
}
