package com.whim.authenticator.totp;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.whim.core.exception.ServiceException;
import com.whim.core.utils.IdUtils;
import com.whim.core.utils.RandomStringUtils;
import com.whim.redis.utils.RedisUtils;
import com.whim.authenticator.recovery.RecoveryCodes;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.digest.DigestUtils;

import java.security.InvalidKeyException;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 管理验证器动态码的绑定申请、密钥保护和 RFC 6238 校验。
 */
@RequiredArgsConstructor
public class TotpVerificationService {
    private static final Base32 BASE32 = Base32.builder().get();
    private static final String SETUP_PREFIX = "auth:twoFactorSetup:";
    private final Supplier<byte[]> masterKey;
    private final TimeBasedOneTimePasswordGenerator generator = new TimeBasedOneTimePasswordGenerator();

    /**
     * 准备一次验证器绑定，返回密钥和恢复码供账号本人保存。
     *
     * @param userId 所属账号
     * @param accountVersion 第一步验证使用的账号凭据版本
     * @return 本次绑定资料
     */
    public TotpEnrollment startEnrollment(Long userId, String accountVersion) {
        String setupId = IdUtils.uuid32();
        String secret = RandomStringUtils.randomByCharacters(32, "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567");
        String recoveryCode = RecoveryCodes.generate();
        // 同一账号只保留最新申请；缓存保存密文和摘要，账号凭据变更会使申请失效。
        Map<String, String> pending = new HashMap<>();
        pending.put("proof", DigestUtils.sha256Hex(accountVersion + setupId));
        pending.put("secret", encryptSecret(userId, secret));
        pending.put("recovery", RecoveryCodes.digest(recoveryCode));
        RedisUtils.setCacheObject(SETUP_PREFIX + userId, pending, Duration.ofMinutes(10));
        return new TotpEnrollment(setupId, secret, recoveryCode);
    }

    /**
     * 核验绑定申请与首次动态码，生成待保存的凭据。
     *
     * @param userId 所属账号
     * @param accountVersion 当前账号凭据版本
     * @param setupId 本次申请编号
     * @param code 首次动态码
     * @return 密钥密文、恢复码摘要和已使用的时间步
     */
    public TotpCredential verifyEnrollment(Long userId, String accountVersion, String setupId, String code) {
        Map<String, String> pending = RedisUtils.getCacheObject(SETUP_PREFIX + userId);
        if (pending == null || !DigestUtils.sha256Hex(accountVersion + setupId).equals(pending.get("proof"))) {
            throw new IllegalArgumentException("绑定申请已失效，请重新验证密码");
        }
        String encryptedSecret = pending.get("secret");
        long lastStep = verifyCode(userId, encryptedSecret, -1L, code);
        return new TotpCredential(encryptedSecret, pending.get("recovery"), lastStep);
    }

    /**
     * 在接入方持有账号锁并保存正式绑定后清理临时申请。
     *
     * @param userId 所属账号
     */
    public void clearEnrollment(Long userId) {
        RedisUtils.deleteObject(SETUP_PREFIX + userId);
    }

    /**
     * 在允许的时钟偏差内验证动态码，返回可原子记录的新时间步。
     *
     * @param userId 密钥所属账号
     * @param encryptedSecret 绑定时保存的密文
     * @param lastStep 已接受的最近时间步
     * @param code 六位动态码
     * @return 本次通过校验的时间步
     */
    public long verifyCode(Long userId, String encryptedSecret, long lastStep, String code) {
        try {
            JWEObject encrypted = JWEObject.parse(encryptedSecret);
            if (!JWEAlgorithm.DIR.equals(encrypted.getHeader().getAlgorithm())
                    || !EncryptionMethod.A256GCM.equals(encrypted.getHeader().getEncryptionMethod())
                    || !userId.toString().equals(encrypted.getHeader().getKeyID())) {
                throw new ServiceException("验证器密钥与账号不匹配");
            }
            encrypted.decrypt(new DirectDecrypter(masterKey.get()));
            byte[] raw = BASE32.decode(encrypted.getPayload().toString());
            OctetSequenceKey key = new OctetSequenceKey.Builder(raw).build();
            long stepSeconds = generator.getTimeStep().toSeconds();
            long currentStep = Instant.now().getEpochSecond() / stepSeconds;
            // 允许前后一个时间步的时钟差；行锁保护的高水位使同一个时间步只能通过一次。
            for (long step = currentStep + 1; step >= currentStep - 1 && step > lastStep; step--) {
                if (generator.validateOneTimePassword(key.toSecretKey(generator.getAlgorithm()),
                        Instant.ofEpochSecond(step * stepSeconds), code)) {
                    return step;
                }
            }
            throw new IllegalArgumentException("动态码无效或已使用，请等待下一枚动态码");
        } catch (ParseException | JOSEException | InvalidKeyException exception) {
            throw new ServiceException("验证器密钥读取失败，请检查两步验证主密钥");
        }
    }

    /**
     * 用认证加密保护密钥，并将账号写入受认证的密文头。
     *
     * @param userId 所属账号
     * @param secret 验证器 Base32 密钥
     * @return 可持久化的密文
     */
    private String encryptSecret(Long userId, String secret) {
        try {
            JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
                    .keyID(userId.toString()).build();
            JWEObject encrypted = new JWEObject(header, new Payload(secret));
            encrypted.encrypt(new DirectEncrypter(masterKey.get()));
            return encrypted.serialize();
        } catch (JOSEException exception) {
            throw new ServiceException("验证器密钥加密失败");
        }
    }
}
