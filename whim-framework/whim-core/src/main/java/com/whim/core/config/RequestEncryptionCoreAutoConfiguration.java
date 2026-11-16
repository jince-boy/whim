package com.whim.core.config;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.RSAKey;
import com.whim.core.config.properties.RequestEncryptionKeyProperties;
import com.whim.core.crypto.RequestEncryptionCipher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Set;

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
                privateKeyBase64 = getPersistentPrivateKey(properties.getPrivateKeyFile());
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

    /**
     * 读取持久化私钥，文件不存在时安全生成并保存，避免重启导致密钥变化。
     *
     * @param privateKeyFile 私钥文件路径
     * @return PKCS#8 DER 单行 Base64 私钥
     * @throws IOException 密钥文件无法读写
     * @throws GeneralSecurityException RSA 密钥生成失败
     */
    private String getPersistentPrivateKey(String privateKeyFile) throws IOException, GeneralSecurityException {
        if (privateKeyFile.isBlank()) {
            throw new IllegalArgumentException("请求加密私钥文件路径不能为空");
        }
        Path keyPath = Path.of(privateKeyFile).toAbsolutePath().normalize();
        boolean posix = keyPath.getFileSystem().supportedFileAttributeViews().contains("posix");
        FileAttribute<?>[] directoryAttributes = posix
                ? new FileAttribute<?>[]{PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"))}
                : new FileAttribute<?>[0];
        FileAttribute<?>[] fileAttributes = posix
                ? new FileAttribute<?>[]{PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))}
                : new FileAttribute<?>[0];
        Files.createDirectories(keyPath.getParent(), directoryAttributes);

        // 共用目录的进程在初始化时互斥，避免同时启动生成不同密钥；锁文件不能删除后重建。
        Path lockPath = keyPath.resolveSibling(keyPath.getFileName() + ".lock");
        try (FileChannel lockChannel = FileChannel.open(lockPath,
                Set.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE), fileAttributes);
             FileLock initializationLock = lockChannel.lock()) {
            if (Files.notExists(keyPath)) {
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(3072);
                byte[] encoded = Base64.getEncoder().encode(generator.generateKeyPair().getPrivate().getEncoded());
                Path temporaryPath = Files.createTempFile(keyPath.getParent(), ".request-encryption-", ".tmp", fileAttributes);
                try {
                    // 先完整写入并刷盘，再原子发布，进程中断不会留下半份有效文件。
                    try (FileChannel output = FileChannel.open(temporaryPath, StandardOpenOption.WRITE)) {
                        ByteBuffer buffer = ByteBuffer.wrap(encoded);
                        while (buffer.hasRemaining()) {
                            output.write(buffer);
                        }
                        output.force(true);
                    }
                    Files.move(temporaryPath, keyPath, StandardCopyOption.ATOMIC_MOVE);
                } finally {
                    Files.deleteIfExists(temporaryPath);
                }
            }
            // 已存在的密钥损坏或不可读时直接启动失败，不能悄悄换钥影响在线请求。
            if (Files.size(keyPath) > 16384) {
                throw new IllegalArgumentException("请求加密私钥文件内容过长");
            }
            return Files.readString(keyPath, StandardCharsets.US_ASCII).strip();
        }
    }
}
