package com.whim.system.service.impl;

import com.whim.core.config.properties.AltchaProperties;
import com.whim.core.exception.ServiceException;
import com.whim.system.model.dto.AltchaCaptchaDTO;
import com.whim.system.service.IAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.altcha.altcha.v1.Altcha;
import org.springframework.stereotype.Service;

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
     * 获取 ALTCHA 验证码挑战。
     *
     * @return ALTCHA 验证码挑战
     */
    @Override
    public AltchaCaptchaDTO getCaptcha() {
        try {
            Altcha.ChallengeOptions options = new Altcha.ChallengeOptions()
                    .algorithm(Altcha.Algorithm.fromString(altchaProperties.getAlgorithm()))
                    .maxNumber(altchaProperties.getMaxNumber())
                    .saltLength(altchaProperties.getSaltLength())
                    .secureRandomNumber(true)
                    .hmacKey(altchaProperties.getHmacSignatureSecret())
                    .expiresInSeconds(altchaProperties.getExpiresInSeconds());
            Altcha.Challenge challenge = Altcha.createChallenge(options);

            AltchaCaptchaDTO captcha = new AltchaCaptchaDTO();
            captcha.setAlgorithm(challenge.algorithm());
            captcha.setChallenge(challenge.challenge());
            captcha.setMaxnumber(challenge.maxnumber());
            captcha.setSalt(challenge.salt());
            captcha.setSignature(challenge.signature());
            return captcha;
        } catch (Exception exception) {
            log.warn("生成 ALTCHA 验证码失败", exception);
            throw new ServiceException("生成验证码失败", exception);
        }
    }
}
