package com.whim.redis.handler;

import org.redisson.config.NameMapper;

/**
 * @author Jince
 * @date 2026/03/30
 * @description Redis Key 前缀处理器，通过 Redisson 的 NameMapper 在所有 key 操作时自动添加/移除前缀。
 */
public class KeyPrefixHandler implements NameMapper {

    private final String keyPrefix;

    /**
     * 创建 Key 前缀处理器
     *
     * @param keyPrefix 前缀字符串，为空则不添加前缀
     */
    public KeyPrefixHandler(String keyPrefix) {
        this.keyPrefix = (keyPrefix == null || keyPrefix.isBlank()) ? "" : keyPrefix + ":";
    }

    /**
     * 为 key 添加前缀
     *
     * @param name 原始 key
     * @return 添加前缀后的 key
     */
    @Override
    public String map(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        if (!keyPrefix.isEmpty() && !name.startsWith(keyPrefix)) {
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
        if (name == null || name.isBlank()) {
            return null;
        }
        if (!keyPrefix.isEmpty() && name.startsWith(keyPrefix)) {
            return name.substring(keyPrefix.length());
        }
        return name;
    }
}
