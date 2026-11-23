package com.whim.authenticator.config;

import com.whim.core.crypto.PersistentKeyFile;
import com.whim.authenticator.totp.TotpVerificationService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.function.SingletonSupplier;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 装配手机验证器的动态码服务及密钥配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(TotpProperties.class)
public class AuthenticatorAutoConfiguration {
    /**
     * 创建验证器动态码服务，在首次加解密时加载并复用主密钥。
     *
     * @param properties 主密钥路径配置
     * @return 验证器动态码服务
     */
    @Bean
    public TotpVerificationService totpVerificationService(TotpProperties properties) {
        // 仅动态码依赖主密钥，未绑定用户的密码登录和已有账号的恢复码找回均可独立完成。
        return new TotpVerificationService(SingletonSupplier.of(() -> loadMasterKey(properties)));
    }

    /**
     * 读取或初始化持久化主密钥，供共享密钥文件的实例校验同一份凭据。
     *
     * @param properties 主密钥路径配置
     * @return 256 位主密钥
     */
    private byte[] loadMasterKey(TotpProperties properties) {
        try {
            String encoded = PersistentKeyFile.loadOrCreate(properties.getKeyFile(), () -> {
                byte[] key = new byte[32];
                new SecureRandom().nextBytes(key);
                return key;
            });
            byte[] key = Base64.getDecoder().decode(encoded);
            if (key.length != 32) {
                throw new IllegalArgumentException("两步验证主密钥长度错误");
            }
            return key;
        } catch (IOException | IllegalArgumentException exception) {
            throw new IllegalStateException("两步验证主密钥加载失败，请检查密钥文件和访问权限");
        }
    }
}
