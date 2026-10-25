package com.whim.web.config.properties;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author Jince
 * @date 2026/10/25
 * @description Web JSON 请求体大小和解密频率限制，密钥由 core 模块管理。
 */
@Data
@Validated
@ConfigurationProperties("whim.request-encryption")
public class RequestEncryptionProperties {

    @Min(value = 1024, message = "加密请求体限制不能小于 1024 字节")
    @Max(value = 1048576, message = "加密请求体限制不能超过 1 MiB")
    private int maxBodyBytes = 65536;

    @Min(value = 1, message = "请求加密限流次数必须大于 0")
    private int requestsPerMinute = 120;
}
