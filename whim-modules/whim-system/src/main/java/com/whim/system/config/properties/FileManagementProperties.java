package com.whim.system.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 文件管理凭据加密配置。
 */
@Getter
@Setter
@ConfigurationProperties("file.management")
public class FileManagementProperties {
    /** Base64 编码的32字节密钥，由环境注入，不写入数据库。 */
    private String secretKey;
}
