package com.whim.system.model.vo.auth;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/09/29
 * @description 登录 GIF 验证码响应参数
 */
@Data
public class LoginCaptchaVO {

    /** 验证码唯一标识。 */
    private String uuid;

    /** GIF 图片的纯 Base64 编码。 */
    private String image;

    /** 验证码剩余有效期，单位为秒。 */
    private long expiresIn;
}
