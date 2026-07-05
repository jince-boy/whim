package com.whim.core.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.altcha.altcha.v2.Altcha;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author Jince
 * @date 2026/07/03
 * @description ALTCHA 验证码配置属性
 */
@Data
@Validated
@AutoConfiguration
@ConditionalOnClass(Altcha.class)
@ConfigurationProperties(prefix = "whim.auth.altcha")
public class AltchaProperties {

    /**
     * 挑战签名密钥
     */
    @NotBlank
    private String hmacSignatureSecret = "whim-altcha-dev-secret";

    /**
     * 验证码算法
     */
    @NotBlank
    private String algorithm = "PBKDF2/SHA-256";

    /**
     * 验证码计算成本
     */
    @Min(1)
    private int cost = 5_000;

    /**
     * 挑战有效秒数
     */
    @Min(1)
    private long expiresInSeconds = 300;
}
