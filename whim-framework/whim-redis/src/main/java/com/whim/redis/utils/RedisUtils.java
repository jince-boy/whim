package com.whim.redis.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.redisson.api.ObjectListener;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RMapAsync;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RSet;
import org.redisson.api.RTopic;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.KeysScanOptions;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/04/02
 * @description Redis 工具类，统一封装 Redisson 的常用操作。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisUtils {

    /**
     * 当前使用的 RedissonClient 实例。
     */
    private static volatile RedissonClient redissonClient;

    /**
     * 获取当前使用的 RedissonClient 实例。
     *
     * @return RedissonClient 实例
     */
    private static RedissonClient client() {
        return requireRedissonClient();
    }

    /**
     * 获取当前使用的 RedissonClient 实例。
     *
     * @return RedissonClient 实例
     */
    public static RedissonClient getClient() {
        return requireRedissonClient();
    }

    /**
     * 替换当前 RedissonClient 实例。
     *
     * @param redissonClient RedissonClient 实例
     */
    public static void setRedissonClient(RedissonClient redissonClient) {
        RedisUtils.redissonClient = Objects.requireNonNull(redissonClient, "参数[redissonClient]不能为空");
    }

    /**
     * 重置当前 RedissonClient 实例。
     */
    public static void resetRedissonClient() {
        redissonClient = null;
    }

    /**
     * 尝试获取一个限流令牌。
     *
     * @param key 限流器键
     * @param rateType 限流类型
     * @param rate 限流速率
     * @param rateInterval 限流周期秒数
     * @return 剩余令牌数，未获取到时返回 -1
     */
    public static long rateLimiter(String key, RateType rateType, int rate, int rateInterval) {
        return rateLimiter(key, rateType, rate, rateInterval, 0);
    }

    /**
     * 尝试获取一个限流令牌并设置超时时间。
     *
     * @param key 限流器键
     * @param rateType 限流类型
     * @param rate 限流速率
     * @param rateInterval 限流周期秒数
     * @param timeout 超时秒数
     * @return 剩余令牌数，未获取到时返回 -1
     */
    public static long rateLimiter(String key, RateType rateType, int rate, int rateInterval, int timeout) {
        RRateLimiter rateLimiter = client().getRateLimiter(key);
        rateLimiter.trySetRate(rateType, rate, Duration.ofSeconds(rateInterval), Duration.ofSeconds(timeout));
        if (rateLimiter.tryAcquire()) {
            return rateLimiter.availablePermits();
        }
        return -1L;
    }

    /**
     * 发布消息到指定主题。
     *
     * @param channelKey 主题键
     * @param message 消息内容
     * @param <T> 消息类型
     */
    public static <T> void publish(String channelKey, T message) {
        RTopic topic = client().getTopic(channelKey);
        topic.publish(message);
    }

    /**
     * 发布消息并回调消费函数。
     *
     * @param channelKey 主题键
     * @param message 消息内容
     * @param consumer 消费回调
     * @param <T> 消息类型
     */
    public static <T> void publish(String channelKey, T message, Consumer<T> consumer) {
        publish(channelKey, message);
        consumer.accept(message);
    }

    /**
     * 订阅指定主题的消息。
     *
     * @param channelKey 主题键
     * @param type 消息类型
     * @param consumer 消息处理器
     * @param <T> 消息类型
     */
    public static <T> void subscribe(String channelKey, Class<T> type, Consumer<T> consumer) {
        RTopic topic = client().getTopic(channelKey);
        topic.addListener(type, (channel, message) -> consumer.accept(message));
    }

    /**
     * 写入对象缓存。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param <T> 值类型
     */
    public static <T> void setCacheObject(String key, T value) {
        client().<T>getBucket(key).set(value);
    }

    /**
     * 写入对象缓存并尽量保留原有 TTL。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param <T> 值类型
     */
    public static <T> void setCacheObjectKeepTTL(String key, T value) {
        RBucket<T> bucket = client().getBucket(key);
        try {
            bucket.setAndKeepTTL(value);
        } catch (Exception exception) {
            long remainTimeToLive = bucket.remainTimeToLive();
            if (remainTimeToLive > 0) {
                bucket.set(value, Duration.ofMillis(remainTimeToLive));
            } else {
                bucket.set(value);
            }
        }
    }

    /**
     * 按指定过期时间写入对象缓存。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param duration 过期时间
     * @param <T> 值类型
     */
    public static <T> void setCacheObject(String key, T value, Duration duration) {
        client().<T>getBucket(key).set(value, duration);
    }

    /**
     * 仅在键不存在时写入对象缓存。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param duration 过期时间
     * @param <T> 值类型
     * @return 是否写入成功
     */
    public static <T> boolean setObjectIfAbsent(String key, T value, Duration duration) {
        return client().<T>getBucket(key).setIfAbsent(value, duration);
    }

    /**
     * 仅在键已存在时写入对象缓存。
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param duration 过期时间
     * @param <T> 值类型
     * @return 是否写入成功
     */
    public static <T> boolean setObjectIfExists(String key, T value, Duration duration) {
        return client().<T>getBucket(key).setIfExists(value, duration);
    }

    /**
     * 读取对象缓存。
     *
     * @param key 缓存键
     * @param <T> 值类型
     * @return 缓存值
     */
    public static <T> T getCacheObject(String key) {
        return client().<T>getBucket(key).get();
    }

    /**
     * 删除指定对象缓存。
     *
     * @param key 缓存键
     * @return 是否删除成功
     */
    public static boolean deleteObject(String key) {
        return client().getBucket(key).delete();
    }

    /**
     * 批量删除对象缓存。
     *
     * @param keys 缓存键集合
     */
    public static void deleteObject(Collection<?> keys) {
        RBatch batch = client().createBatch();
        keys.forEach(key -> batch.getBucket(String.valueOf(key)).deleteAsync());
        batch.execute();
    }

    /**
     * 判断对象缓存是否存在。
     *
     * @param key 缓存键
     * @return 是否存在
     */
    public static boolean isExistsObject(String key) {
        return client().getBucket(key).isExists();
    }

    /**
     * 为对象缓存添加监听器。
     *
     * @param key 缓存键
     * @param listener 监听器
     */
    public static void addObjectListener(String key, ObjectListener listener) {
        client().getBucket(key).addListener(listener);
    }

    /**
     * 设置键的过期时间。
     *
     * @param key 缓存键
     * @param timeout 过期秒数
     * @return 是否设置成功
     */
    public static boolean expire(String key, long timeout) {
        return expire(key, Duration.ofSeconds(timeout));
    }

    /**
     * 设置键的过期时间。
     *
     * @param key 缓存键
     * @param duration 过期时间
     * @return 是否设置成功
     */
    public static boolean expire(String key, Duration duration) {
        return client().getBucket(key).expire(duration);
    }

    /**
     * 获取键的剩余过期时间。
     *
     * @param key 缓存键
     * @return 剩余毫秒数
     */
    public static long getTimeToLive(String key) {
        return client().getBucket(key).remainTimeToLive();
    }

    /**
     * 向列表缓存追加多个元素。
     *
     * @param key 缓存键
     * @param dataList 数据集合
     * @param <T> 元素类型
     * @return 是否追加成功
     */
    public static <T> boolean setCacheList(String key, List<T> dataList) {
        RList<T> rList = client().getList(key);
        return rList.addAll(dataList);
    }

    /**
     * 向列表缓存追加单个元素。
     *
     * @param key 缓存键
     * @param data 数据
     * @param <T> 元素类型
     * @return 是否追加成功
     */
    public static <T> boolean addCacheList(String key, T data) {
        RList<T> rList = client().getList(key);
        return rList.add(data);
    }

    /**
     * 读取列表缓存的全部元素。
     *
     * @param key 缓存键
     * @param <T> 元素类型
     * @return 列表数据
     */
    public static <T> List<T> getCacheList(String key) {
        RList<T> rList = client().getList(key);
        return rList.readAll();
    }

    /**
     * 按区间读取列表缓存。
     *
     * @param key 缓存键
     * @param from 起始下标
     * @param to 结束下标
     * @param <T> 元素类型
     * @return 列表区间数据
     */
    public static <T> List<T> getCacheListRange(String key, int from, int to) {
        RList<T> rList = client().getList(key);
        return rList.range(from, to);
    }

    /**
     * 为列表缓存添加监听器。
     *
     * @param key 缓存键
     * @param listener 监听器
     */
    public static void addListListener(String key, ObjectListener listener) {
        client().getList(key).addListener(listener);
    }

    /**
     * 向集合缓存追加多个元素。
     *
     * @param key 缓存键
     * @param dataSet 数据集合
     * @param <T> 元素类型
     * @return 是否追加成功
     */
    public static <T> boolean setCacheSet(String key, Set<T> dataSet) {
        RSet<T> rSet = client().getSet(key);
        return rSet.addAll(dataSet);
    }

    /**
     * 向集合缓存追加单个元素。
     *
     * @param key 缓存键
     * @param data 数据
     * @param <T> 元素类型
     * @return 是否追加成功
     */
    public static <T> boolean addCacheSet(String key, T data) {
        RSet<T> rSet = client().getSet(key);
        return rSet.add(data);
    }

    /**
     * 读取集合缓存的全部元素。
     *
     * @param key 缓存键
     * @param <T> 元素类型
     * @return 集合数据
     */
    public static <T> Set<T> getCacheSet(String key) {
        RSet<T> rSet = client().getSet(key);
        return rSet.readAll();
    }

    /**
     * 为集合缓存添加监听器。
     *
     * @param key 缓存键
     * @param listener 监听器
     */
    public static void addSetListener(String key, ObjectListener listener) {
        client().getSet(key).addListener(listener);
    }

    /**
     * 向哈希缓存写入多个字段。
     *
     * @param key 缓存键
     * @param dataMap 字段数据
     * @param <T> 值类型
     */
    public static <T> void setCacheMap(String key, Map<String, T> dataMap) {
        if (dataMap != null && !dataMap.isEmpty()) {
            client().<String, T>getMap(key).putAll(dataMap);
        }
    }

    /**
     * 读取哈希缓存的全部字段。
     *
     * @param key 缓存键
     * @param <T> 值类型
     * @return 哈希数据
     */
    public static <T> Map<String, T> getCacheMap(String key) {
        RMap<String, T> rMap = client().getMap(key);
        return rMap.getAll(rMap.keySet());
    }

    /**
     * 读取哈希缓存的字段集合。
     *
     * @param key 缓存键
     * @return 字段集合
     */
    public static Set<String> getCacheMapKeySet(String key) {
        RMap<String, ?> rMap = client().getMap(key);
        return rMap.keySet();
    }

    /**
     * 写入哈希缓存的单个字段。
     *
     * @param key 缓存键
     * @param hashKey 字段键
     * @param value 字段值
     * @param <T> 值类型
     */
    public static <T> void setCacheMapValue(String key, String hashKey, T value) {
        client().<String, T>getMap(key).put(hashKey, value);
    }

    /**
     * 读取哈希缓存的单个字段。
     *
     * @param key 缓存键
     * @param hashKey 字段键
     * @param <T> 值类型
     * @return 字段值
     */
    public static <T> T getCacheMapValue(String key, String hashKey) {
        RMap<String, T> rMap = client().getMap(key);
        return rMap.get(hashKey);
    }

    /**
     * 批量读取哈希缓存字段。
     *
     * @param key 缓存键
     * @param hashKeys 字段键集合
     * @param <T> 值类型
     * @return 字段数据
     */
    public static <T> Map<String, T> getMultiCacheMapValue(String key, Set<String> hashKeys) {
        RMap<String, T> rMap = client().getMap(key);
        return rMap.getAll(hashKeys);
    }

    /**
     * 删除哈希缓存的单个字段。
     *
     * @param key 缓存键
     * @param hashKey 字段键
     * @param <T> 值类型
     * @return 被删除的字段值
     */
    public static <T> T delCacheMapValue(String key, String hashKey) {
        RMap<String, T> rMap = client().getMap(key);
        return rMap.remove(hashKey);
    }

    /**
     * 批量删除哈希缓存字段。
     *
     * @param key 缓存键
     * @param hashKeys 字段键集合
     */
    public static void delMultiCacheMapValue(String key, Set<String> hashKeys) {
        RBatch batch = client().createBatch();
        RMapAsync<String, ?> rMap = batch.getMap(key);
        hashKeys.forEach(rMap::removeAsync);
        batch.execute();
    }

    /**
     * 为哈希缓存添加监听器。
     *
     * @param key 缓存键
     * @param listener 监听器
     */
    public static void addMapListener(String key, ObjectListener listener) {
        client().getMap(key).addListener(listener);
    }

    /**
     * 设置原子计数值。
     *
     * @param key 缓存键
     * @param value 数值
     */
    public static void setAtomicValue(String key, long value) {
        RAtomicLong atomicLong = client().getAtomicLong(key);
        atomicLong.set(value);
    }

    /**
     * 读取原子计数值。
     *
     * @param key 缓存键
     * @return 当前数值
     */
    public static long getAtomicValue(String key) {
        RAtomicLong atomicLong = client().getAtomicLong(key);
        return atomicLong.get();
    }

    /**
     * 原子递增计数值。
     *
     * @param key 缓存键
     * @return 递增后的数值
     */
    public static long incrAtomicValue(String key) {
        RAtomicLong atomicLong = client().getAtomicLong(key);
        return atomicLong.incrementAndGet();
    }

    /**
     * 原子递减计数值。
     *
     * @param key 缓存键
     * @return 递减后的数值
     */
    public static long decrAtomicValue(String key) {
        RAtomicLong atomicLong = client().getAtomicLong(key);
        return atomicLong.decrementAndGet();
    }

    /**
     * 按模式扫描键集合。
     *
     * @param pattern 键模式
     * @return 键集合
     */
    public static Collection<String> keys(String pattern) {
        return keys(KeysScanOptions.defaults().pattern(pattern).chunkSize(1000));
    }

    /**
     * 按扫描参数获取键集合。
     *
     * @param options 扫描参数
     * @return 键集合
     */
    public static Collection<String> keys(KeysScanOptions options) {
        return client().getKeys().getKeysStream(options).collect(Collectors.toList());
    }

    /**
     * 按模式删除键集合。
     *
     * @param pattern 键模式
     */
    public static void deleteKeys(String pattern) {
        client().getKeys().deleteByPattern(pattern);
    }

    /**
     * 判断键是否存在。
     *
     * @param key 键
     * @return 是否存在
     */
    public static boolean hasKey(String key) {
        RKeys rKeys = client().getKeys();
        return rKeys.countExists(key) > 0;
    }

    /**
     * 获取当前 RedissonClient，未初始化时抛出异常。
     *
     * @return RedissonClient 实例
     */
    private static RedissonClient requireRedissonClient() {
        if (redissonClient == null) {
            throw new IllegalStateException("RedisUtils 尚未初始化 RedissonClient");
        }
        return redissonClient;
    }

    /**
     * @author Jince
     * @date 2026/04/02
     * @description RedisUtils 初始化器，用于接入 Spring 管理的 RedissonClient。
     */
    public static final class Initializer {

        /**
         * 初始化 RedisUtils 使用的 RedissonClient。
         *
         * @param redissonClient Spring 管理的 RedissonClient
         */
        public Initializer(RedissonClient redissonClient) {
            RedisUtils.setRedissonClient(redissonClient);
        }
    }
}
