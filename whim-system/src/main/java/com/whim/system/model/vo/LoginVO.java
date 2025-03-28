package com.whim.system.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Jince
 * date: 2024/11/10 23:56
 * description: 用户登录响应实体类
 */
@Data
@AllArgsConstructor
public class LoginVO {
    /**
     * token 前缀
     */
    private String prefix;
    /**
     * token
     */
    private String token;
    /**
     * 有效时间
     */
    private Long expires;
}
