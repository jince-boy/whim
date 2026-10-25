package com.whim.core.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 请求解密私钥配置，与前端配置的固定公钥配对使用。
 */
@Getter
@Setter
@ConfigurationProperties("whim.request-encryption")
public class RequestEncryptionKeyProperties {

    /** PKCS#8 DER 编码的 RSA 私钥，配置为无头尾标记和换行的 Base64 字符串；不生成包含私钥的 toString 方法。 */
    private String privateKey = "";
}
