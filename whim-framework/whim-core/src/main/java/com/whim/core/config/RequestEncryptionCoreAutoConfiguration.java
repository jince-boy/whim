package com.whim.core.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.RSAKey;
import com.whim.core.config.properties.RequestEncryptionKeyProperties;
import com.whim.core.crypto.PersistentKeyFile;
import com.whim.core.crypto.RequestEncryptionCipher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 加载或自动生成持久化 RSA 密钥，装配请求加解密能力。
 */
@AutoConfiguration
@EnableConfigurationProperties(RequestEncryptionKeyProperties.class)
public class RequestEncryptionCoreAutoConfiguration {

    /**
     * 优先使用外部私钥，否则加载持久化私钥，首次启动时自动生成。
     *
     * @param properties 请求加密密钥配置
     * @return 请求解密组件
     */
    @Bean
    public RequestEncryptionCipher requestEncryptionCipher(RequestEncryptionKeyProperties properties) {
        try {
            String privateKeyBase64 = properties.getPrivateKey().strip();
            if (privateKeyBase64.isEmpty()) {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(3072);
                privateKeyBase64 = PersistentKeyFile.loadOrCreate(Path.of(properties.getPrivateKeyFile()),
                        () -> generator.generateKeyPair().getPrivate().getEncoded());
            }
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
            return new RequestEncryptionCipher(key, new RSADecrypter(key),
                    Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        } catch (IOException | GeneralSecurityException | JOSEException | RuntimeException exception) {
            // 不把配置中的私钥或解析器的原始异常写入启动日志。
            throw new IllegalStateException("请求加密初始化失败：请检查私钥文件目录的读写权限，或确认已配置的私钥为至少 3072 位 RSA 的 PKCS#8 单行 Base64；已有文件无效时不会自动覆盖");
        }
    }
}
