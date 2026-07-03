package com.whim.system.model.dto;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/07/03
 * @description ALTCHA 验证码响应参数
 */
@Data
public class AltchaCaptchaDTO {

    /**
     * 验证码算法
     */
    private String algorithm;

    /**
     * 验证码挑战值
     */
    private String challenge;

    /**
     * 最大尝试数字
     */
    private Long maxnumber;

    /**
     * 随机盐值
     */
    private String salt;

    /**
     * 验证码挑战签名
     */
    private String signature;
}
