package com.whim.system.model.vo.auth;

import lombok.Data;

/**
 * @author jince
 * date: 2026/7/5 16:33
 * description: 登录响应参数
 */
@Data
public class AuthLoginVO {
    /**
     * 认证前缀
     */
    private String prefix;
    /**
     * 认证令牌
     */
    private String token;
    /**
     * 认证令牌有效期
     */
    private Long expires;
}
