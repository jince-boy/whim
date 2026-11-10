package com.whim.system.config;

import com.whim.system.config.properties.FileManagementProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 注册文件管理配置。
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
@EnableConfigurationProperties(FileManagementProperties.class)
public class FileManagementConfiguration {
}
