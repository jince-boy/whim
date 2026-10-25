package com.whim.core.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.RSAKey;
import com.whim.core.config.properties.RequestEncryptionKeyProperties;
import com.whim.core.crypto.RequestEncryptionCipher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 从配置中的固定 RSA 私钥装配 JWE 解密能力。
 */
@AutoConfiguration
@EnableConfigurationProperties(RequestEncryptionKeyProperties.class)
public class RequestEncryptionCoreAutoConfiguration {

    /**
     * 从单行 Base64 私钥初始化请求解密组件，缺少配置或配置无效时终止启动。
     *
     * @param properties 请求加密密钥配置
     * @return 请求解密组件
     */
    @Bean
    public RequestEncryptionCipher requestEncryptionCipher(RequestEncryptionKeyProperties properties) {
        try {
            String privateKeyBase64 = properties.getPrivateKey().strip();
            if (privateKeyBase64.isEmpty() || privateKeyBase64.length() > 16384) {
                throw new IllegalArgumentException("请求解密私钥必须是有效的单行 Base64 字符串");
            }
            byte[] encoded = Base64.getDecoder().decode(privateKeyBase64);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) factory.generatePrivate(new PKCS8EncodedKeySpec(encoded));
            RSAPublicKey publicKey = (RSAPublicKey) factory.generatePublic(
                    new RSAPublicKeySpec(privateKey.getModulus(), privateKey.getPublicExponent()));
            if (privateKey.getModulus().bitLength() < 3072) {
                throw new IllegalArgumentException("请求加密 RSA 私钥长度不能小于 3072 位");
            }
            RSAKey key = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
            key = new RSAKey.Builder(key).keyID(key.computeThumbprint().toString()).build();
            return new RequestEncryptionCipher(key, new RSADecrypter(key));
        } catch (GeneralSecurityException | JOSEException | RuntimeException exception) {
            // 不把配置中的私钥或解析器的原始异常写入启动日志。
            throw new IllegalStateException("请求加密初始化失败：whim.request-encryption.private-key 必须配置至少 3072 位 RSA 私钥的 PKCS#8 单行 Base64 字符串，并在前端配置对应公钥");
        }
    }
}
