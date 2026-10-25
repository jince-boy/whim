package com.whim.core.crypto;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;

import java.text.ParseException;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 请求 JWE 的密码学校验与解密，不依赖 Servlet、MVC、Redis 或业务模块。
 */
@RequiredArgsConstructor
public class RequestEncryptionCipher {
    private final RSAKey key;
    private final RSADecrypter decrypter;

    /**
     * 校验固定协议和当前密钥并解密 JWE，不泄露原始密码学异常。
     *
     * @param encryptedData JWE Compact 格式密文
     * @return 解密后的载荷字节
     */
    public byte[] decryptRequest(String encryptedData) {
        try {
            JWEObject encrypted = JWEObject.parse(encryptedData);
            var header = encrypted.getHeader();
            if (!JWEAlgorithm.RSA_OAEP_256.equals(header.getAlgorithm())
                    || !EncryptionMethod.A256GCM.equals(header.getEncryptionMethod())
                    || !key.getKeyID().equals(header.getKeyID())
                    || header.getType() == null || !"whim-request+jwe".equals(header.getType().toString())
                    || !"application/json".equals(header.getContentType())
                    || header.getCompressionAlgorithm() != null
                    || (header.getCriticalParams() != null && !header.getCriticalParams().isEmpty())) {
                throw new IllegalArgumentException("请求密文头部错误");
            }
            encrypted.decrypt(decrypter);
            return encrypted.getPayload().toBytes();
        } catch (ParseException | JOSEException | RuntimeException exception) {
            throw new IllegalArgumentException("请求解密失败，请检查请求格式和加密公钥");
        }
    }
}
