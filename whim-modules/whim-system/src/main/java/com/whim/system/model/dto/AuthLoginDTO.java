package com.whim.system.model.dto;

import lombok.Data;

/**
 * @author jince
 * @date 2026/7/3
 * @description
 */
@Data
public class AuthLoginDTO {
    private String username;
    private String password;
    private String captcha;
    private String uuid;
    private String altcha;
}
