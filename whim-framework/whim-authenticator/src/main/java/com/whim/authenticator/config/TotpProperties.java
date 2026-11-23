package com.whim.authenticator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 验证器动态码使用的持久化主密钥配置。
 */
@Getter
@Setter
@ConfigurationProperties("whim.two-factor")
public class TotpProperties {
    private Path keyFile = Path.of("./data/keys/two-factor.key");
}
