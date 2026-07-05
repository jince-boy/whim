package com.whim.system.model.vo.auth;

import lombok.Data;

import java.util.Map;

/**
 * @author Jince
 * @date 2026/07/03
 * @description ALTCHA 验证码响应参数
 */
@Data
public class AltchaCaptchaVO {

    /**
     * 验证码挑战参数
     */
    private Map<String, Object> parameters;

    /**
     * 验证码挑战签名
     */
    private String signature;
}
