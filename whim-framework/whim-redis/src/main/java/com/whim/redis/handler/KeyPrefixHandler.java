package com.whim.redis.handler;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.NameMapper;

/**
 * @author jince
 * date: 2025/6/23 16:59
 * description: key前缀处理
 */
public class KeyPrefixHandler implements NameMapper {
    private final String keyPrefix;

    public KeyPrefixHandler(String keyPrefix) {
        this.keyPrefix = StringUtils.isBlank(keyPrefix) ? "" : keyPrefix + ":";
    }

    @Override
    public String map(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        if (StringUtils.isNotBlank(keyPrefix) && !s.startsWith(keyPrefix)) {
            return keyPrefix + s;
        }
        return s;
    }

    @Override
    public String unmap(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        if (StringUtils.isNotBlank(keyPrefix) && s.startsWith(keyPrefix)) {
            return s.substring(keyPrefix.length());
        }
        return s;
    }
}
