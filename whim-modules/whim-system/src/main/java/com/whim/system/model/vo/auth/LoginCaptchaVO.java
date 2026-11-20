package com.whim.system.model.vo.auth;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 登录 GIF 验证码响应参数
 */
@Data
public class LoginCaptchaVO {
    /** 当前是否要求登录验证码。 */
    private boolean enabled;

    /** 验证码唯一标识。 */
    private String uuid;

    /** 启用时签发的 GIF 图片 Base64 数据。 */
    private String image;

    /** 验证码剩余有效期，单位为秒。 */
    private long expiresIn;
}
