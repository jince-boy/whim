package com.whim.file.config;

import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author jince
 * date: 2025/3/12 18:36
 * description: Tika配置
 */
@Configuration
public class TikaConfig {
    @Bean
    public Tika tika() {
        return new Tika();
    }
}
