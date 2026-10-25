package com.whim.web.config;

import com.whim.core.config.RequestEncryptionCoreAutoConfiguration;
import com.whim.core.crypto.RequestEncryptionCipher;
import com.whim.web.config.properties.RequestEncryptionProperties;
import com.whim.web.filter.RequestDecryptionFilter;
import com.whim.web.security.RequestEncryptionService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tools.jackson.databind.json.JsonMapper;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 将 core 的请求解密能力接入 Web 请求过滤流程。
 */
@AutoConfiguration(after = RequestEncryptionCoreAutoConfiguration.class)
@EnableConfigurationProperties(RequestEncryptionProperties.class)
public class RequestEncryptionAutoConfiguration {

    /**
     * 注册 JSON 请求解析及 JWE 解密服务。
     *
     * @param properties 请求解密配置
     * @param cipher core 提供的请求密码学组件
     * @param jsonMapper JSON 映射器
     * @return 请求加密服务
     */
    @Bean
    public RequestEncryptionService requestEncryptionService(
            RequestEncryptionProperties properties,
            RequestEncryptionCipher cipher, JsonMapper jsonMapper) {
        return new RequestEncryptionService(properties, cipher, jsonMapper);
    }

    /**
     * 注册在 XSS 清洗之前执行的请求解密过滤器。
     *
     * @param service 请求加密服务
     * @param handlerMapping MVC 路由映射
     * @param exceptionResolver 全局异常解析器
     * @return 请求解密过滤器注册信息
     */
    @Bean
    public FilterRegistrationBean<RequestDecryptionFilter> requestDecryptionFilter(
            RequestEncryptionService service,
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        var registration = new FilterRegistrationBean<RequestDecryptionFilter>();
        registration.setFilter(new RequestDecryptionFilter(service, handlerMapping, exceptionResolver));
        registration.setName("requestDecryptionFilter");
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
