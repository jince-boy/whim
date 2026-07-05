package com.whim.system.service.impl;

import com.whim.core.config.properties.AltchaProperties;
import com.whim.core.exception.ServiceException;
import com.whim.system.model.dto.auth.AuthLoginDTO;
import com.whim.system.model.vo.auth.AltchaCaptchaVO;
import com.whim.system.model.vo.auth.AuthLoginVO;
import com.whim.system.service.IAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.altcha.altcha.v2.Altcha;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jince
 * @date 2026/7/3
 * @description 认证服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    /**
     * ALTCHA 验证码配置
     */
    private final AltchaProperties altchaProperties;

    /**
     * 登录。
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    @Override
    public AuthLoginVO login(AuthLoginDTO loginDTO) {
        Altcha.VerifySolutionResult result;
        try {
            result = Altcha.verifySolution(
                    loginDTO.getAltcha(),
                    altchaProperties.getHmacSignatureSecret(),
                    Altcha.kdf(altchaProperties.getAlgorithm()));
        } catch (Exception exception) {
            throw new ServiceException("验证码校验失败", exception);
        }
        if (!result.verified()) {
            log.warn("验证码校验失败，expired={}, invalidSignature={}, invalidSolution={}",
                    result.expired(), result.invalidSignature(), result.invalidSolution());
            throw new ServiceException("验证码校验失败");
        }
        return null;
    }

    /**
     * 获取 ALTCHA 验证码挑战。
     *
     * @return ALTCHA 验证码挑战
     */
    @Override
    public AltchaCaptchaVO getCaptcha() {
        try {
            Altcha.CreateChallengeOptions options = new Altcha.CreateChallengeOptions()
                    .algorithm(altchaProperties.getAlgorithm())
                    .cost(altchaProperties.getCost())
                    .hmacSignatureSecret(altchaProperties.getHmacSignatureSecret())
                    .expiresInSeconds(altchaProperties.getExpiresInSeconds());
            Altcha.Challenge challenge = Altcha.createChallenge(options);

            AltchaCaptchaVO captcha = new AltchaCaptchaVO();
            captcha.setParameters(buildChallengeParameters(challenge.parameters()));
            captcha.setSignature(challenge.signature());
            return captcha;
        } catch (Exception exception) {
            log.warn("生成 ALTCHA 验证码失败", exception);
            throw new ServiceException("生成验证码失败", exception);
        }
    }

    /**
     * 构建 ALTCHA v2 验证码挑战参数，避免向前端输出空字段。
     *
     * @param parameters ALTCHA 挑战参数
     * @return 验证码挑战参数
     */
    private Map<String, Object> buildChallengeParameters(Altcha.ChallengeParameters parameters) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("algorithm", parameters.algorithm());
        result.put("cost", parameters.cost());
        if (parameters.data() != null) {
            result.put("data", parameters.data());
        }
        if (parameters.expiresAt() != null) {
            result.put("expiresAt", parameters.expiresAt());
        }
        result.put("keyLength", parameters.keyLength());
        result.put("keyPrefix", parameters.keyPrefix());
        if (parameters.keySignature() != null) {
            result.put("keySignature", parameters.keySignature());
        }
        if (parameters.memoryCost() != null) {
            result.put("memoryCost", parameters.memoryCost());
        }
        result.put("nonce", parameters.nonce());
        if (parameters.parallelism() != null) {
            result.put("parallelism", parameters.parallelism());
        }
        result.put("salt", parameters.salt());
        return result;
    }
}
