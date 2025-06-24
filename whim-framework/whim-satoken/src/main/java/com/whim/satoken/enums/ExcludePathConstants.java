package com.whim.satoken.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jince
 * date: 2024/10/24 23:13
 * description: 排除路径枚举类
 */
@Getter
public enum ExcludePathConstants {
    INDEX_PATH("/", "首页"),
    AUTH_LOGIN("/system/auth/login", "用户登录"),
    AUTH_CAPTCHA("/system/auth/captcha", "登录验证码"),
    ERROR_PATH("/error", "springboot框架Error路径"),
    ;
    private final String path;
    private final String description;

    ExcludePathConstants(String path, String description) {
        this.path = path;
        this.description = description;
    }

    /**
     * 获取所有的枚举
     *
     * @return List<String>
     */
    public static List<String> getAllEnumDetails() {
        List<String> enumDetails = new ArrayList<>();
        for (ExcludePathConstants value : ExcludePathConstants.values()) {
            enumDetails.add(value.getPath());
        }
        return enumDetails;
    }
}
