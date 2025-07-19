package com.whim.json.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.whim.json.handler.BigNumberSerializer;
import com.whim.json.handler.DateSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author jince
 * @date 2025/6/17 20:46
 * @description Jackson 配置
 */
@Slf4j
@AutoConfiguration(before = JacksonAutoConfiguration.class)
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            // 1. 时间模块：仅处理时间类型
            JavaTimeModule timeModule = new JavaTimeModule();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
            timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
            timeModule.addDeserializer(Date.class, new DateSerializer()); // 假设这是自定义的Date反序列化器

            // 2. 数字模块：处理大数字类型
            SimpleModule numberModule = new SimpleModule();
            numberModule.addSerializer(Long.class, BigNumberSerializer.INSTANCE);
            numberModule.addSerializer(long.class, BigNumberSerializer.INSTANCE);
            numberModule.addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE);
            numberModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);

            // 注册所有模块
            builder.modules(timeModule, numberModule);
            builder.timeZone(TimeZone.getDefault());
        };
    }
}
