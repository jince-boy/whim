package com.whim.redis.handler;

import org.redisson.config.NameMapper;
import org.springframework.util.StringUtils;

/**
 * @author Jince
 * @date 2026/03/30
 * @description Redis Key 前缀处理器，通过 Redisson 的 NameMapper 在所有 key 操作时自动添加/移除前缀。
 */
public final class KeyPrefixHandler implements NameMapper {

    private final String keyPrefix;

    /**
     * 创建 Key 前缀处理器
     *
     * @param keyPrefix 前缀字符串，为空则不添加前缀
     */
    public KeyPrefixHandler(String keyPrefix) {
        this.keyPrefix = normalizePrefix(keyPrefix);
    }

    /**
     * 为 key 添加前缀
     *
     * @param name 原始 key
     * @return 添加前缀后的 key
     */
    @Override
    public String map(String name) {
        if (!StringUtils.hasText(name) || keyPrefix.isEmpty()) {
            return name;
        }
        if (!name.startsWith(keyPrefix)) {
            return keyPrefix + name;
        }
        return name;
    }

    /**
     * 移除 key 的前缀
     *
     * @param name 带前缀的 key
     * @return 移除前缀后的原始 key
     */
    @Override
    public String unmap(String name) {
        if (!StringUtils.hasText(name) || keyPrefix.isEmpty()) {
            return name;
        }
        if (name.startsWith(keyPrefix)) {
            return name.substring(keyPrefix.length());
        }
        return name;
    }

    /**
     * 规范化 key 前缀，统一去除尾部冒号后再补齐层级分隔符。
     *
     * @param keyPrefix 原始前缀
     * @return 规范化后的前缀
     */
    private String normalizePrefix(String keyPrefix) {
        if (!StringUtils.hasText(keyPrefix)) {
            return "";
        }
        String normalizedPrefix = keyPrefix.trim();
        while (normalizedPrefix.endsWith(":")) {
            normalizedPrefix = normalizedPrefix.substring(0, normalizedPrefix.length() - 1);
        }
        if (!StringUtils.hasText(normalizedPrefix)) {
            return "";
        }
        return normalizedPrefix + ":";
    }
}
