package com.whim.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * @author Jince
 * @date 2026/3/24
 * @description ID 生成工具类，当前仅提供与具体持久化框架无关的 UUID 生成能力。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IdUtils {

    /**
     * 生成标准 UUID。
     *
     * @return 形如 {@code xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx} 的字符串。
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成不带横线的 UUID。
     *
     * @return 32 位 UUID 字符串。
     */
    public static String uuid32() {
        return uuid().replace("-", "");
    }
}
