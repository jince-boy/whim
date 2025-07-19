package com.whim.system.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Jince
 * @date 2024/10/26 15:14
 * @description 验证码响应实体类
 */
@Data
@AllArgsConstructor
public class CaptchaVO {
    /**
     * uuid
     */
    private String uuid;
    /**
     * base64 图片
     */
    private String base64;
}
