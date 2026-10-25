package com.whim.web.security;

import com.whim.core.crypto.RequestEncryptionCipher;
import com.whim.core.exception.TooManyRequestsException;
import com.whim.core.utils.IPUtils;
import com.whim.redis.utils.RedisUtils;
import com.whim.web.config.properties.RequestEncryptionProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RateType;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/10/25
 * @description 识别 JSON 密文并解密，按后端注解强制要求密文，不依赖前端加密标识。
 */
@RequiredArgsConstructor
public class RequestEncryptionService {
    private static final Set<String> BODY_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final RequestEncryptionProperties properties;
    private final RequestEncryptionCipher cipher;
    private final JsonMapper jsonMapper;

    /**
     * 读取有长度上限的 JSON 请求，解密密文或保留未要求加密的原始请求体。
     *
     * @param request 当前请求
     * @param encryptionRequired 接口是否通过注解强制要求密文
     * @return 可交给 MVC 反序列化的业务 JSON 字节
     * @throws IOException 读取请求流失败时抛出
     */
    public byte[] getRequestBody(HttpServletRequest request, boolean encryptionRequired) throws IOException {
        if (!BODY_METHODS.contains(request.getMethod()) || request.getContentType() == null
                || !MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(request.getContentType()))
                || request.getHeader("Content-Encoding") != null) {
            throw new IllegalArgumentException("请求解密仅支持无内容编码的 POST、PUT、PATCH、DELETE JSON 请求体");
        }
        byte[] requestBytes = request.getInputStream().readNBytes(properties.getMaxBodyBytes() + 1);
        if (requestBytes.length > properties.getMaxBodyBytes()) {
            throw new IllegalArgumentException("JSON 请求体超过允许的大小");
        }
        JsonNode envelope;
        try {
            envelope = jsonMapper.readTree(requestBytes);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("JSON 请求体格式错误");
        }
        if (envelope == null || !envelope.isObject() || !envelope.has("encryptedData")) {
            if (encryptionRequired) {
                throw new IllegalArgumentException("当前接口必须提交加密的 JSON 请求体");
            }
            return requestBytes;
        }
        if (envelope.size() != 1 || !envelope.get("encryptedData").isString()
                || request.getQueryString() != null) {
            throw new IllegalArgumentException("加密请求仅接受 encryptedData 字符串，业务参数必须放在 JSON 请求体中");
        }
        if (RedisUtils.rateLimiter("web:requestEncryption:rate:decrypt:" + IPUtils.getClientIpAddress(request),
                RateType.OVERALL, properties.getRequestsPerMinute(), 60, 120) < 0) {
            throw new TooManyRequestsException("加密请求过于频繁，请稍后重试");
        }
        JsonNode payload;
        try {
            payload = jsonMapper.readTree(cipher.decryptRequest(envelope.get("encryptedData").stringValue()));
        } catch (RuntimeException exception) {
            // 不向日志或客户端传播包含请求明文、密文或密码学细节的原始异常。
            throw new IllegalArgumentException("请求解密失败，请检查请求格式和加密公钥");
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (payload == null || !payload.isObject() || payload.size() != 3
                || !payload.path("method").isString() || !request.getMethod().equals(payload.get("method").stringValue())
                || !payload.path("path").isString() || !path.equals(payload.get("path").stringValue())
                || (!payload.path("body").isObject() && !payload.path("body").isArray())) {
            throw new IllegalArgumentException("加密请求内容或目标接口不匹配");
        }
        return jsonMapper.writeValueAsString(payload.get("body")).getBytes(StandardCharsets.UTF_8);
    }
}
