package com.whim.redis.utils;

import com.whim.core.utils.SpringUtils;
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
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Redis 工具类，统一封装 Redisson 的常用操作。
 */
public final class RedisUtils {

    private RedisUtils() {
    }

    private static RedissonClient client() {
        return SpringUtils.getBean(RedissonClient.class);
    }

    public static long rateLimiter(String key, RateType rateType, int rate, int rateInterval) {
        return rateLimiter(key, rateType, rate, rateInterval, 0);
    }

    public static long rateLimiter(String key, RateType rateType, int rate, int rateInterval, int timeout) {
        RRateLimiter rateLimiter = client().getRateLimiter(key);
        rateLimiter.trySetRate(rateType, rate, Duration.ofSeconds(rateInterval), Duration.ofSeconds(timeout));
        if (rateLimiter.tryAcquire()) {
            return rateLimiter.availablePermits();
        }
        return -1L;
    }

    public static RedissonClient getClient() {
        return client();
    }

    public static <T> void publish(String channelKey, T message) {
        RTopic topic = client().getTopic(channelKey);
        topic.publish(message);
    }

    public static <T> void publish(String channelKey, T message, Consumer<T> consumer) {
        publish(channelKey, message);
        consumer.accept(message);
    }

    public static <T> void subscribe(String channelKey, Class<T> type, Consumer<T> consumer) {
        RTopic topic = client().getTopic(channelKey);
        topic.addListener(type, (channel, message) -> consumer.accept(message));
    }

    public static <T> void setCacheObject(String key, T value) {
        client().<T>getBucket(key).set(value);
    }

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

    public static <T> void setCacheObject(String key, T value, Duration duration) {
        client().<T>getBucket(key).set(value, duration);
    }

    public static <T> boolean setObjectIfAbsent(String key, T value, Duration duration) {
        return client().<T>getBucket(key).setIfAbsent(value, duration);
    }

    public static <T> boolean setObjectIfExists(String key, T value, Duration duration) {
        return client().<T>getBucket(key).setIfExists(value, duration);
    }

    public static <T> T getCacheObject(String key) {
        return client().<T>getBucket(key).get();
    }

    public static boolean deleteObject(String key) {
        return client().getBucket(key).delete();
    }

    public static void deleteObject(Collection<?> keys) {
        RBatch batch = client().createBatch();
        keys.forEach(key -> batch.getBucket(String.valueOf(key)).deleteAsync());
        batch.execute();
    }

    public static boolean isExistsObject(String key) {
        return client().getBucket(key).isExists();
    }

    public static void addObjectListener(String key, ObjectListener listener) {
        client().getBucket(key).addListener(listener);
    }

    public static boolean expire(String key, long timeout) {
        return expire(key, Duration.ofSeconds(timeout));
    }

    public static boolean expire(String key, Duration duration) {
        return client().getBucket(key).expire(duration);
    }

    public static long getTimeToLive(String key) {
        return client().getBucket(key).remainTimeToLive();
    }

    public static <T> boolean setCacheList(String key, List<T> dataList) {
        RList<T> rList = client().getList(key);
        return rList.addAll(dataList);
    }

    public static <T> boolean addCacheList(String key, T data) {
        RList<T> rList = client().getList(key);
        return rList.add(data);
    }

    public static <T> List<T> getCacheList(String key) {
        RList<T> rList = client().getList(key);
        return rList.readAll();
    }

    public static <T> List<T> getCacheListRange(String key, int from, int to) {
        RList<T> rList = client().getList(key);
        return rList.range(from, to);
    }

    public static void addListListener(String key, ObjectListener listener) {
        client().getList(key).addListener(listener);
    }

    public static <T> boolean setCacheSet(String key, Set<T> dataSet) {
        RSet<T> rSet = client().getSet(key);
        return rSet.addAll(dataSet);
    }

    public static <T> boolean addCacheSet(String key, T data) {
        RSet<T> rSet = client().getSet(key);
        return rSet.add(data);
    }

    public static <T> Set<T> getCacheSet(String key) {
        RSet<T> rSet = client().getSet(key);
        return rSet.readAll();
    }

    public static void addSetListener(String key, ObjectListener listener) {
        client().getSet(key).addListener(listener);
    }

    public static <T> void setCacheMap(String key, Map<String, T> dataMap) {
        if (dataMap != null && !dataMap.isEmpty()) {
            client().<String, T>getMap(key).putAll(dataMap);
        }
    }

    public static <T> Map<String, T> getCacheMap(String key) {
        RMap<String, T> rMap = client().getMap(key);
        return rMap.getAll(rMap.keySet());
    }

    public static Set<String> getCacheMapKeySet(String key) {
        RMap<String, ?> rMap = client().getMap(key);
        return rMap.keySet();
    }

    public static <T> void setCacheMapValue(String key, String hashKey, T value) {
        client().<String, T>getMap(key).put(hashKey, value);
    }

    public static <T> T getCacheMapValue(String key, String hashKey) {
        RMap<String, T> rMap = client().getMap(key);
        return rMap.get(hashKey);
    }

    public static <T> Map<String, T> getMultiCacheMapValue(String key, Set<String> hashKeys) {
        RMap<String, T> rMap = client().getMap(key);
        return rMap.getAll(hashKeys);
    }

    public static <T> T delCacheMapValue(String key, String hashKey) {
        RMap<String, T> rMap = client().getMap(key);
        return rMap.remove(hashKey);
    }

    public static void delMultiCacheMapValue(String key, Set<String> hashKeys) {
        RBatch batch = client().createBatch();
        RMapAsync<String, ?> rMap = batch.getMap(key);
        hashKeys.forEach(rMap::removeAsync);
        batch.execute();
    }

    public static void addMapListener(String key, ObjectListener listener) {
        client().getMap(key).addListener(listener);
    }

    public static void setAtomicValue(String key, long value) {
        RAtomicLong atomicLong = client().getAtomicLong(key);
        atomicLong.set(value);
    }

    public static long getAtomicValue(String key) {
        RAtomicLong atomicLong = client().getAtomicLong(key);
        return atomicLong.get();
    }

    public static long incrAtomicValue(String key) {
        RAtomicLong atomicLong = client().getAtomicLong(key);
        return atomicLong.incrementAndGet();
    }

    public static long decrAtomicValue(String key) {
        RAtomicLong atomicLong = client().getAtomicLong(key);
        return atomicLong.decrementAndGet();
    }

    public static Collection<String> keys(String pattern) {
        return keys(KeysScanOptions.defaults().pattern(pattern).chunkSize(1000));
    }

    public static Collection<String> keys(KeysScanOptions options) {
        return client().getKeys().getKeysStream(options).collect(Collectors.toList());
    }

    public static void deleteKeys(String pattern) {
        client().getKeys().deleteByPattern(pattern);
    }

    public static boolean hasKey(String key) {
        RKeys rKeys = client().getKeys();
        return rKeys.countExists(key) > 0;
    }
}
