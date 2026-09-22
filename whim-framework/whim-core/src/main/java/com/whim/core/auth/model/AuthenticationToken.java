package com.whim.core.auth.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author Jince
 * @date 2026/09/22
 * @description 认证成功后的通用令牌信息。
 */
@Data
public class AuthenticationToken implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 令牌类型
     */
    private String tokenType;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 剩余有效期，单位为秒
     */
    private Long expiresIn;
}
