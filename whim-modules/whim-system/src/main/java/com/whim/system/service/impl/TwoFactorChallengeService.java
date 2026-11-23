package com.whim.system.service.impl;

import com.whim.core.exception.TooManyRequestsException;
import com.whim.core.utils.IdUtils;
import com.whim.redis.utils.RedisUtils;
import com.whim.system.model.entity.SysUser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.redisson.api.RateType;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 管理系统账号两步验证的临时登录凭证及尝试次数。
 */
@Service
class TwoFactorChallengeService {
    private static final String LOGIN_PREFIX = "auth:twoFactorLogin:";
    private static final String ATTEMPTS_PREFIX = "auth:twoFactorAttempts:";

    /**
     * 签发与客户端地址及当前账号安全状态绑定的短时登录凭证。
     *
     * @param user 已通过密码验证的系统账号
     * @param bindingId 密码验证时的验证器绑定编号
     * @param clientAddress 发起请求的客户端地址
     * @return 客户端继续验证时使用的凭证编号
     */
    String issueLoginChallenge(SysUser user, Long bindingId, String clientAddress) {
        String challengeId = IdUtils.uuid32();
        Map<String, String> challenge = new HashMap<>();
        challenge.put("userId", user.getId().toString());
        challenge.put("proof", getSecurityProof(user.getPassword(), bindingId));
        RedisUtils.setCacheObject(LOGIN_PREFIX + challengeId + ":" + clientAddress, challenge, Duration.ofMinutes(3));
        return challengeId;
    }

    /**
     * 读取同一客户端发起的登录凭证，用于在第二步验证前锁定系统账号。
     *
     * @param challengeId 凭证编号
     * @param clientAddress 当前客户端地址
     * @return 尚未消费的登录凭证
     */
    LoginChallenge getLoginChallenge(String challengeId, String clientAddress) {
        String key = LOGIN_PREFIX + challengeId + ":" + clientAddress;
        Map<String, String> challenge = RedisUtils.getCacheObject(key);
        if (challenge == null) {
            throw new IllegalArgumentException("登录验证已过期，请返回密码登录");
        }
        return new LoginChallenge(key, Long.valueOf(challenge.get("userId")), challenge.get("proof"));
    }

    /**
     * 限制同一账号在登录验证、绑定和找回过程中的尝试总数。
     *
     * @param userId 尝试验证的账号
     */
    void limitAttempts(Long userId) {
        if (RedisUtils.rateLimiter(ATTEMPTS_PREFIX + userId, RateType.OVERALL, 8, 60, 120) < 0) {
            throw new TooManyRequestsException("验证过于频繁，请稍后再试");
        }
    }

    /**
     * 将临时凭证绑定到密码和当前验证器，改密或重新绑定会使原凭证失效。
     *
     * @param passwordHash 账号当前的密码摘要
     * @param bindingId 验证器绑定编号
     * @return 登录安全状态摘要
     */
    private static String getSecurityProof(String passwordHash, Long bindingId) {
        return DigestUtils.sha256Hex(passwordHash + ":" + bindingId);
    }

    /**
     * @author Jince
     * @date 2026/11/22
     * @description 已读取的登录凭证，验证成功后须消费才能签发会话。
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static final class LoginChallenge {
        private final String key;
        @Getter
        private final Long userId;
        private final String proof;

        /**
         * 判断第一步验证所依据的账号安全状态是否仍然有效。
         *
         * @param passwordHash 持有账号锁时读取的密码摘要
         * @param bindingId 当前验证器绑定编号
         * @return 是否与签发时一致
         */
        boolean matchesSecurityState(String passwordHash, Long bindingId) {
            return getSecurityProof(passwordHash, bindingId).equals(proof);
        }

        /**
         * 在后续验证通过时原子消费凭证，阻止并发重用或过期凭证换取会话。
         */
        void consume() {
            if (RedisUtils.takeCacheObject(key) == null) {
                throw new IllegalArgumentException("登录验证已过期，请返回密码登录");
            }
        }
    }
}
