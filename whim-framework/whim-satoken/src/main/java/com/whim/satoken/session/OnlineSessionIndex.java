package com.whim.satoken.session;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.listener.SaTokenListenerForSimple;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.dev33.satoken.strategy.SaStrategy;
import cn.dev33.satoken.util.SaValue2Box;
import com.whim.core.auth.model.OnlineSessionInfo;
import com.whim.core.auth.model.UserInfo;
import com.whim.core.utils.DateUtils;
import com.whim.core.utils.ValueParserUtils;
import com.whim.satoken.constants.AuthSessionKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.redisson.api.RBatch;
import org.redisson.api.RLock;
import org.redisson.api.RSetCache;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.KeysScanOptions;
import org.redisson.client.RedisException;
import org.redisson.client.codec.StringCodec;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Gatherers;
import java.util.stream.Stream;

/**
 * @author Jince
 * @date 2026/11/23
 * @description 维护 Redis 在线会话索引，并从原生认证记录分批读取有效会话。
 */
@RequiredArgsConstructor
@Slf4j
public class OnlineSessionIndex extends SaTokenListenerForSimple {
    private static final int BATCH_SIZE = 128;
    private static final String READY = "@index-ready";
    private final RedissonClient redisson;
    private final Set<String> repairs = ConcurrentHashMap.newKeySet();

    /**
     * 将新签发的令牌加入对应账号体系的索引。
     *
     * @param loginType 账号体系
     * @param loginId 账号ID
     * @param tokenValue 登录令牌
     * @param loginParameter 本次登录配置
     */
    @Override
    public void doLogin(String loginType, Object loginId, String tokenValue, SaLoginParameter loginParameter) {
        updateSessionLifetime(loginType, tokenValue, loginParameter.getTimeout());
    }

    /**
     * 主动注销时移除会话索引。
     *
     * @param loginType 账号体系
     * @param loginId 账号ID
     * @param tokenValue 被撤销的令牌
     */
    @Override
    public void doLogout(String loginType, Object loginId, String tokenValue) {
        updateSessionLifetime(loginType, tokenValue, 0);
    }

    /**
     * 强退或授权失效时移除会话索引。
     *
     * @param loginType 账号体系
     * @param loginId 账号ID
     * @param tokenValue 被撤销的令牌
     */
    @Override
    public void doKickout(String loginType, Object loginId, String tokenValue) {
        updateSessionLifetime(loginType, tokenValue, 0);
    }

    /**
     * 终端被新登录顶替时移除会话索引。
     *
     * @param loginType 账号体系
     * @param loginId 账号ID
     * @param tokenValue 被撤销的令牌
     */
    @Override
    public void doReplaced(String loginType, Object loginId, String tokenValue) {
        updateSessionLifetime(loginType, tokenValue, 0);
    }

    /**
     * 令牌主动续期时，同步索引成员的失效时间。
     *
     * @param loginType 账号体系
     * @param loginId 账号ID
     * @param tokenValue 已续期的令牌
     * @param timeout 新有效期秒数
     */
    @Override
    public void doRenewTimeout(String loginType, Object loginId, String tokenValue, long timeout) {
        updateSessionLifetime(loginType, tokenValue, timeout);
    }

    /**
     * 按令牌的硬有效期维护索引，空闲冻结由查询时的认证状态决定。
     *
     * @param loginType 账号体系
     * @param tokenValue 登录令牌
     * @param timeout 有效期秒数
     */
    private void updateSessionLifetime(String loginType, String tokenValue, long timeout) {
        RSetCache<String> index = getIndex(SaManager.getStpLogic(loginType));
        try {
            if (timeout == SaTokenDao.NEVER_EXPIRE) {
                index.add(tokenValue);
            } else if (timeout > 0) {
                index.add(tokenValue, timeout, TimeUnit.SECONDS);
            } else {
                index.remove(tokenValue);
            }
        } catch (RedisException exception) {
            // 监控写入失败不能中断 Sa-Token 对同一账号其余令牌的强退。
            repairs.add(loginType);
            log.warn("在线会话索引更新失败，等待校准，账号体系={}，异常类型={}", loginType, exception.getClass().getSimpleName());
        }
    }

    /**
     * 按索引分批读取指定账号体系的有效会话，索引缺失时从认证记录恢复。
     *
     * @param logic 账号体系认证逻辑
     * @return 只读的有效会话流
     */
    Stream<OnlineSessionInfo> getOnlineSessions(StpLogic logic) {
        RSetCache<String> index = getIndex(logic);
        if (repairs.remove(logic.getLoginType())) {
            try {
                index.remove(READY);
            } catch (RedisException exception) {
                repairs.add(logic.getLoginType());
                throw exception;
            }
        }
        if (!index.contains(READY)) {
            initializeIndex(logic, index);
        }
        String currentToken = logic.getTokenValue();
        return index.stream(BATCH_SIZE).filter(token -> !READY.equals(token)).distinct()
                .gather(Gatherers.windowFixed(BATCH_SIZE))
                .flatMap(tokens -> getSessionBatch(logic, tokens, currentToken).stream());
    }

    /**
     * 合并读取会话、登录映射及最后活动记录，查询过程中不续期。
     *
     * @param logic 账号体系认证逻辑
     * @param tokens 本批令牌
     * @param currentToken 当前操作者的令牌
     * @return 具有有效身份快照的会话
     */
    List<OnlineSessionInfo> getSessionBatch(StpLogic logic, List<String> tokens, String currentToken) {
        List<String> keys = new ArrayList<>(tokens.size() * 3);
        for (String token : tokens) {
            keys.add(logic.splicingKeyTokenSession(token));
            keys.add(logic.splicingKeyTokenValue(token));
            keys.add(logic.splicingKeyLastActiveTime(token));
        }
        // 与项目 SaTokenDaoForRedisson 的字符串编码及会话反序列化策略保持一致。
        Map<String, String> values = redisson.getBuckets(StringCodec.INSTANCE).get(keys.toArray(String[]::new));
        List<OnlineSessionInfo> sessions = new ArrayList<>(tokens.size());
        long now = System.currentTimeMillis();
        for (String token : tokens) {
            String owner = values.get(logic.splicingKeyTokenValue(token));
            String payload = values.get(logic.splicingKeyTokenSession(token));
            if (payload == null || !logic.isValidLoginId(owner)) {
                continue;
            }
            SaValue2Box activity = new SaValue2Box(values.get(logic.splicingKeyLastActiveTime(token)));
            Long lastActive = activity.getValue1AsLong(null);
            long allowedIdle = logic.getConfigOrGlobal().getActiveTimeout();
            if (logic.getConfigOrGlobal().getDynamicActiveTimeout()) {
                allowedIdle = activity.getValue2AsLong(allowedIdle);
            }
            // Sa-Token 以整秒计算空闲时间；只留下认证拦截器仍会接受的令牌。
            if (logic.isOpenCheckActiveTimeout() && (lastActive == null
                    || allowedIdle != SaTokenDao.NEVER_EXPIRE && (now - lastActive) / 1000 > allowedIdle)) {
                continue;
            }
            SaSession session = SaManager.getSaSerializerTemplate().stringToObject(payload, SaStrategy.instance.sessionClassType);
            if (!(session.get(AuthSessionKeys.LOGIN_USER) instanceof UserInfo user)
                    || !owner.equals(String.valueOf(user.getUserId()))) {
                continue;
            }
            OnlineSessionInfo info = new OnlineSessionInfo();
            info.setSessionId(DigestUtils.sha256Hex(token));
            info.setUserId(user.getUserId());
            info.setDeptId(user.getDeptId());
            info.setUsername(user.getUsername());
            info.setClientType(ValueParserUtils.toString(session.get(AuthSessionKeys.LOGIN_CLIENT_TYPE)));
            info.setDeviceType(ValueParserUtils.toString(session.get(AuthSessionKeys.LOGIN_DEVICE_TYPE)));
            info.setName(user.getName());
            info.setDeptName(user.getDepartmentInfo() == null ? null : user.getDepartmentInfo().getDeptName());
            info.setIpAddress(ValueParserUtils.toString(session.get(AuthSessionKeys.LOGIN_IP)));
            info.setBrowser(ValueParserUtils.toString(session.get(AuthSessionKeys.LOGIN_BROWSER)));
            info.setOs(ValueParserUtils.toString(session.get(AuthSessionKeys.LOGIN_OS)));
            info.setLoginTime(DateUtils.toLocalDateTime(new Date(session.getCreateTime())));
            info.setLastActiveTime(lastActive == null ? null : DateUtils.toLocalDateTime(new Date(lastActive)));
            info.setCurrent(token.equals(currentToken));
            sessions.add(info);
        }
        return sessions;
    }

    /**
     * 在索引缺失时协调多实例回填。
     *
     * @param logic 账号体系认证逻辑
     * @param index 待填充的会话索引
     */
    private void initializeIndex(StpLogic logic, RSetCache<String> index) {
        RLock lock = redisson.getLock(index.getName() + ":initialize");
        lock.lock();
        try {
            if (!index.contains(READY)) {
                // 新登录同步入索引；冷启动和周期校准时补入旧版本实例签发或监听器漏写的令牌。
                String prefix = logic.splicingKeyTokenSession("");
                try (Stream<String> keys = redisson.getKeys().getKeysStream(
                        KeysScanOptions.defaults().pattern(prefix + "*").chunkSize(BATCH_SIZE))) {
                    keys.gather(Gatherers.windowFixed(BATCH_SIZE)).forEach(batch -> addExistingSessions(index, prefix, batch));
                }
                // 标记与成员同键，回填成功才设置。一小时内的常规刷新只遍历会话集合。
                index.add(READY, 1, TimeUnit.HOURS);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 根据原会话的实际剩余寿命补入升级前已登录的终端。
     *
     * @param index 在线会话索引
     * @param prefix 令牌会话键前缀
     * @param keys 本批原生会话键
     */
    private void addExistingSessions(RSetCache<String> index, String prefix, List<String> keys) {
        RBatch batch = redisson.createBatch();
        for (String key : keys) {
            batch.getBucket(key, StringCodec.INSTANCE).remainTimeToLiveAsync();
        }
        List<?> lifetimes = batch.execute().getResponses();
        Map<String, Duration> entries = new HashMap<>();
        for (int position = 0; position < keys.size(); position++) {
            long remaining = (Long) lifetimes.get(position);
            if (remaining >= 0 || remaining == SaTokenDao.NEVER_EXPIRE) {
                entries.put(keys.get(position).substring(prefix.length()),
                        remaining == SaTokenDao.NEVER_EXPIRE ? Duration.ZERO : Duration.ofMillis(Math.max(1, remaining)));
            }
        }
        if (!entries.isEmpty()) {
            index.addAllIfAbsent(entries);
        }
    }

    /**
     * 获取当前账号体系独立的缓存集合。
     *
     * @param logic 账号体系认证逻辑
     * @return 会话索引
     */
    private RSetCache<String> getIndex(StpLogic logic) {
        String key = logic.getConfigOrGlobal().getTokenName() + ":" + logic.getLoginType() + ":online-index";
        return redisson.getSetCache(key, StringCodec.INSTANCE);
    }
}
