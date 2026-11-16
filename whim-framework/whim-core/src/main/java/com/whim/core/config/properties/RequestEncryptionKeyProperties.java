package com.whim.core.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 请求解密密钥配置，默认自动生成并持久保存私钥。
 */
@Getter
@Setter
@ConfigurationProperties("whim.request-encryption")
public class RequestEncryptionKeyProperties {

    /** 可选的外部私钥，使用 PKCS#8 DER 单行 Base64；配置后优先使用，不写入密钥文件。 */
    private String privateKey = "";

    /** 自动生成的私钥文件，必须保存在可持久化且不对外提供文件访问的目录。 */
    private String privateKeyFile = "./data/keys/request-encryption-private.key";
}
