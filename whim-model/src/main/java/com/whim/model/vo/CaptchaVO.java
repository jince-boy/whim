package com.whim.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Jince
 * date: 2024/10/26 15:14
 * description: 验证码响应实体类
 */
@Data
@Accessors(chain = true)
public class CaptchaVO {
    private String uuid;
    private String base64;
}
