package com.whim.redis.service;

import lombok.RequiredArgsConstructor;
import org.redisson.api.ObjectListener;
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
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Jince
 * @date 2026/03/30
 * @description Redis 操作服务，基于 Redisson 封装常用操作。通过 Spring 依赖注入使用。
 */
@RequiredArgsConstructor
public class RedisService {

    private final RedissonClient redissonClient;

    /**
     * 获取 RedissonClient 实例
     *
     * @return RedissonClient
     */
    public RedissonClient getClient() {
        return redissonClient;
    }

    // ================================ 限流 ================================

    /**
     * 限流
     *
     * @param key          限流 key
     * @param rateType     限流类型（全局/单机）
     * @param rate         速率（允许的请求数）
     * @param rateInterval 速率间隔（秒）
     * @return 剩余可用许可数，-1 表示获取失败
     */
    public long rateLimiter(String key, RateType rateType, int rate, int rateInterval) {
        return rateLimiter(key, rateType, rate, rateInterval, 0);
    }

    /**
     * 限流（带超时）
     *
     * @param key          限流 key
     * @param rateType     限流类型
     * @param rate         速率
     * @param rateInterval 速率间隔（秒）
     * @param timeout      限流器过期时间（秒），0 表示永不过期
     * @return 剩余可用许可数，-1 表示获取失败
     */
    public long rateLimiter(String key, RateType rateType, int rate, int rateInterval, int timeout) {
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.trySetRate(rateType, rate, Duration.ofSeconds(rateInterval), Duration.ofSeconds(timeout));
        if (rateLimiter.tryAcquire()) {
            return rateLimiter.availablePermits();
        }
        return -1L;
    }

    // ================================ 发布/订阅 ================================

    /**
     * 发布消息到指定频道
     *
     * @param channelKey 频道 key
     * @param message    消息内容
     * @param <T>        消息类型
     */
    public <T> void publish(String channelKey, T message) {
        RTopic topic = redissonClient.getTopic(channelKey);
        topic.publish(message);
    }

    /**
     * 发布消息到指定频道，并执行自定义后处理
     *
     * @param channelKey 频道 key
     * @param message    消息内容
     * @param consumer   发布后的回调处理
     * @param <T>        消息类型
     */
    public <T> void publish(String channelKey, T message, Consumer<T> consumer) {
        RTopic topic = redissonClient.getTopic(channelKey);
        topic.publish(message);
        consumer.accept(message);
    }

    /**
     * 订阅频道消息
     *
     * @param channelKey 频道 key
     * @param clazz      消息类型
     * @param consumer   消息处理器
     * @param <T>        消息类型
     */
    public <T> void subscribe(String channelKey, Class<T> clazz, Consumer<T> consumer) {
        RTopic topic = redissonClient.getTopic(channelKey);
        topic.addListener(clazz, (channel, msg) -> consumer.accept(msg));
    }

    // ================================ 基本对象操作 ================================

    /**
     * 缓存对象
     *
     * @param key   缓存 key
     * @param value 缓存值
     * @param <T>   值类型
     */
    public <T> void setCacheObject(String key, T value) {
        redissonClient.<T>getBucket(key).set(value);
    }

    /**
     * 缓存对象并保留当前 TTL
     *
     * @param key   缓存 key
     * @param value 缓存值
     * @param <T>   值类型
     */
    public <T> void setCacheObjectKeepTTL(String key, T value) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        try {
            bucket.setAndKeepTTL(value);
        } catch (Exception e) {
            long remainTimeToLive = bucket.remainTimeToLive();
            if (remainTimeToLive > 0) {
                bucket.set(value, Duration.ofMillis(remainTimeToLive));
            } else {
                bucket.set(value);
            }
        }
    }

    /**
     * 缓存对象并设置过期时间
     *
     * @param key      缓存 key
     * @param value    缓存值
     * @param duration 过期时间
     * @param <T>      值类型
     */
    public <T> void setCacheObject(String key, T value, Duration duration) {
        redissonClient.<T>getBucket(key).set(value, duration);
    }

    /**
     * 如果 key 不存在则设置，成功返回 true
     *
     * @param key      缓存 key
     * @param value    缓存值
     * @param duration 过期时间
     * @param <T>      值类型
     * @return 是否设置成功
     */
    public <T> boolean setObjectIfAbsent(String key, T value, Duration duration) {
        return redissonClient.<T>getBucket(key).setIfAbsent(value, duration);
    }

    /**
     * 如果 key 存在则设置，成功返回 true
     *
     * @param key      缓存 key
     * @param value    缓存值
     * @param duration 过期时间
     * @param <T>      值类型
     * @return 是否设置成功
     */
    public <T> boolean setObjectIfExists(String key, T value, Duration duration) {
        return redissonClient.<T>getBucket(key).setIfExists(value, duration);
    }

    /**
     * 获取缓存对象
     *
     * @param key 缓存 key
     * @param <T> 值类型
     * @return 缓存值
     */
    public <T> T getCacheObject(String key) {
        return redissonClient.<T>getBucket(key).get();
    }

    /**
     * 删除缓存对象
     *
     * @param key 缓存 key
     * @return 是否删除成功
     */
    public boolean deleteObject(String key) {
        return redissonClient.getBucket(key).delete();
    }

    /**
     * 批量删除缓存对象
     *
     * @param keys key 集合
     */
    public void deleteObject(Collection<?> keys) {
        RBatch batch = redissonClient.createBatch();
        keys.forEach(key -> batch.getBucket(key.toString()).deleteAsync());
        batch.execute();
    }

    /**
     * 检查缓存 key 是否存在
     *
     * @param key 缓存 key
     * @return 是否存在
     */
    public boolean isExistsObject(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    /**
     * 注册对象监听器（需 Redis 开启 notify-keyspace-events）
     *
     * @param key      缓存 key
     * @param listener 监听器
     */
    public void addObjectListener(String key, ObjectListener listener) {
        redissonClient.getBucket(key).addListener(listener);
    }

    // ================================ 过期时间 ================================

    /**
     * 设置过期时间（秒）
     *
     * @param key     缓存 key
     * @param timeout 超时秒数
     * @return 是否设置成功
     */
    public boolean expire(String key, long timeout) {
        return expire(key, Duration.ofSeconds(timeout));
    }

    /**
     * 设置过期时间
     *
     * @param key      缓存 key
     * @param duration 过期时间
     * @return 是否设置成功
     */
    public boolean expire(String key, Duration duration) {
        return redissonClient.getBucket(key).expire(duration);
    }

    /**
     * 获取 key 剩余存活时间（毫秒）
     *
     * @param key 缓存 key
     * @return 剩余存活毫秒数，-1 表示永不过期，-2 表示 key 不存在
     */
    public long getTimeToLive(String key) {
        return redissonClient.getBucket(key).remainTimeToLive();
    }

    // ================================ List 操作 ================================

    /**
     * 缓存 List 数据
     *
     * @param key      缓存 key
     * @param dataList 数据列表
     * @param <T>      元素类型
     * @return 是否成功
     */
    public <T> boolean setCacheList(String key, List<T> dataList) {
        RList<T> rList = redissonClient.getList(key);
        return rList.addAll(dataList);
    }

    /**
     * 向 List 追加单个元素
     *
     * @param key  缓存 key
     * @param data 数据
     * @param <T>  元素类型
     * @return 是否成功
     */
    public <T> boolean addCacheList(String key, T data) {
        RList<T> rList = redissonClient.getList(key);
        return rList.add(data);
    }

    /**
     * 获取完整的 List 缓存
     *
     * @param key 缓存 key
     * @param <T> 元素类型
     * @return 数据列表
     */
    public <T> List<T> getCacheList(String key) {
        RList<T> rList = redissonClient.getList(key);
        return rList.readAll();
    }

    /**
     * 获取 List 缓存的指定范围
     *
     * @param key  缓存 key
     * @param from 起始下标（含）
     * @param to   结束下标（含）
     * @param <T>  元素类型
     * @return 范围内的数据列表
     */
    public <T> List<T> getCacheListRange(String key, int from, int to) {
        RList<T> rList = redissonClient.getList(key);
        return rList.range(from, to);
    }

    /**
     * 注册 List 监听器（需 Redis 开启 notify-keyspace-events）
     *
     * @param key      缓存 key
     * @param listener 监听器
     */
    public void addListListener(String key, ObjectListener listener) {
        redissonClient.getList(key).addListener(listener);
    }

    // ================================ Set 操作 ================================

    /**
     * 缓存 Set 数据
     *
     * @param key     缓存 key
     * @param dataSet 数据集合
     * @param <T>     元素类型
     * @return 是否成功
     */
    public <T> boolean setCacheSet(String key, Set<T> dataSet) {
        RSet<T> rSet = redissonClient.getSet(key);
        return rSet.addAll(dataSet);
    }

    /**
     * 向 Set 追加单个元素
     *
     * @param key  缓存 key
     * @param data 数据
     * @param <T>  元素类型
     * @return 是否成功
     */
    public <T> boolean addCacheSet(String key, T data) {
        RSet<T> rSet = redissonClient.getSet(key);
        return rSet.add(data);
    }

    /**
     * 获取完整的 Set 缓存
     *
     * @param key 缓存 key
     * @param <T> 元素类型
     * @return 数据集合
     */
    public <T> Set<T> getCacheSet(String key) {
        RSet<T> rSet = redissonClient.getSet(key);
        return rSet.readAll();
    }

    /**
     * 注册 Set 监听器（需 Redis 开启 notify-keyspace-events）
     *
     * @param key      缓存 key
     * @param listener 监听器
     */
    public void addSetListener(String key, ObjectListener listener) {
        redissonClient.getSet(key).addListener(listener);
    }

    // ================================ Map / Hash 操作 ================================

    /**
     * 缓存 Map 数据
     *
     * @param key     缓存 key
     * @param dataMap 数据 Map
     * @param <T>     值类型
     */
    public <T> void setCacheMap(String key, Map<String, T> dataMap) {
        if (dataMap != null) {
            redissonClient.<String, T>getMap(key).putAll(dataMap);
        }
    }

    /**
     * 获取完整的 Map 缓存
     *
     * @param key 缓存 key
     * @param <T> 值类型
     * @return 数据 Map
     */
    public <T> Map<String, T> getCacheMap(String key) {
        RMap<String, T> rMap = redissonClient.getMap(key);
        return rMap.getAll(rMap.keySet());
    }

    /**
     * 获取 Map 所有的 key
     *
     * @param key 缓存 key
     * @return key 集合
     */
    public Set<String> getCacheMapKeySet(String key) {
        RMap<String, ?> rMap = redissonClient.getMap(key);
        return rMap.keySet();
    }

    /**
     * 设置 Hash 中的单个字段
     *
     * @param key   Redis key
     * @param hKey  Hash 字段名
     * @param value 字段值
     * @param <T>   值类型
     */
    public <T> void setCacheMapValue(String key, String hKey, T value) {
        redissonClient.<String, T>getMap(key).put(hKey, value);
    }

    /**
     * 获取 Hash 中的单个字段值
     *
     * @param key  Redis key
     * @param hKey Hash 字段名
     * @param <T>  值类型
     * @return 字段值
     */
    public <T> T getCacheMapValue(String key, String hKey) {
        RMap<String, T> rMap = redissonClient.getMap(key);
        return rMap.get(hKey);
    }

    /**
     * 批量获取 Hash 中的字段值
     *
     * @param key   Redis key
     * @param hKeys Hash 字段名集合
     * @param <T>   值类型
     * @return 字段名到值的映射
     */
    public <T> Map<String, T> getMultiCacheMapValue(String key, Set<String> hKeys) {
        RMap<String, T> rMap = redissonClient.getMap(key);
        return rMap.getAll(hKeys);
    }

    /**
     * 删除 Hash 中的单个字段
     *
     * @param key  Redis key
     * @param hKey Hash 字段名
     * @param <T>  值类型
     * @return 被删除的字段值
     */
    public <T> T delCacheMapValue(String key, String hKey) {
        RMap<String, T> rMap = redissonClient.getMap(key);
        return rMap.remove(hKey);
    }

    /**
     * 批量删除 Hash 中的字段
     *
     * @param key   Redis key
     * @param hKeys Hash 字段名集合
     */
    public void delMultiCacheMapValue(String key, Set<String> hKeys) {
        RBatch batch = redissonClient.createBatch();
        RMapAsync<String, ?> rMap = batch.getMap(key);
        for (String hKey : hKeys) {
            rMap.removeAsync(hKey);
        }
        batch.execute();
    }

    /**
     * 注册 Map 监听器（需 Redis 开启 notify-keyspace-events）
     *
     * @param key      缓存 key
     * @param listener 监听器
     */
    public void addMapListener(String key, ObjectListener listener) {
        redissonClient.getMap(key).addListener(listener);
    }

    // ================================ 原子计数器 ================================

    /**
     * 设置原子值
     *
     * @param key   Redis key
     * @param value 值
     */
    public void setAtomicValue(String key, long value) {
        redissonClient.getAtomicLong(key).set(value);
    }

    /**
     * 获取原子值
     *
     * @param key Redis key
     * @return 当前值
     */
    public long getAtomicValue(String key) {
        return redissonClient.getAtomicLong(key).get();
    }

    /**
     * 原子递增并返回新值
     *
     * @param key Redis key
     * @return 递增后的值
     */
    public long incrAtomicValue(String key) {
        return redissonClient.getAtomicLong(key).incrementAndGet();
    }

    /**
     * 原子递减并返回新值
     *
     * @param key Redis key
     * @return 递减后的值
     */
    public long decrAtomicValue(String key) {
        return redissonClient.getAtomicLong(key).decrementAndGet();
    }

    // ================================ Key 扫描 ================================

    /**
     * 按模式扫描 key 集合
     *
     * @param pattern 匹配模式（如 user:*）
     * @return 匹配的 key 集合
     */
    public Collection<String> keys(String pattern) {
        return keys(KeysScanOptions.defaults().pattern(pattern).chunkSize(1000));
    }

    /**
     * 按自定义扫描参数获取 key 集合
     *
     * @param options 扫描参数
     * @return 匹配的 key 集合
     * @see KeysScanOptions
     */
    public Collection<String> keys(KeysScanOptions options) {
        return redissonClient.getKeys().getKeysStream(options).collect(Collectors.toList());
    }

    /**
     * 按模式批量删除 key
     *
     * @param pattern 匹配模式
     */
    public void deleteKeys(String pattern) {
        redissonClient.getKeys().deleteByPattern(pattern);
    }

    /**
     * 检查 key 是否存在
     *
     * @param key 缓存 key
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        RKeys rKeys = redissonClient.getKeys();
        return rKeys.countExists(key) > 0;
    }
}
