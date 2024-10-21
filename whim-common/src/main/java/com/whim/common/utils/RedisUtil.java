package com.whim.common.utils;

import io.lettuce.core.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author Jince
 * date: 2024/10/20 01:11
 * description: Redis工具类
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存基本对象
     *
     * @param key   键
     * @param value 值
     * @param <T>   Object
     * @return Boolean true=设置成功；false=设置失败
     */
    public <T> Boolean setObject(String key, T value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.info(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 缓存
     *
     * @param key      键
     * @param value    值
     * @param timeout  时间
     * @param timeUnit 时间单位
     * @param <T>      Object
     * @return Boolean true=设置成功；false=设置失败
     */
    public <T> Boolean setObject(String key, T value, Long timeout, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
        } catch (Exception e) {
            log.info(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 设置缓存的失效时间
     *
     * @param key      键
     * @param timeout  时间
     * @param timeUnit 时间单位
     * @return true=设置成功；false=设置失败
     */
    public Boolean setExpire(String key, Long timeout, TimeUnit timeUnit) {
        try {
            if (timeout > 0) {
                redisTemplate.expire(key, timeout, timeUnit);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 获取键的有效时间
     *
     * @param key 键
     * @return Long 秒
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     *
     * @param key 键
     * @return true=存在；false=不存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 获取缓存对象
     *
     * @param key 键
     * @return Object 对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getObject(String key) {
        ValueOperations<String, T> operations = (ValueOperations<String, T>) redisTemplate.opsForValue();
        return operations.get(key);
    }

    /**
     * 删除缓存对象
     *
     * @param key 键
     * @return true=删除成功；false=删除失败
     */
    @SuppressWarnings("unchecked")
    public Boolean deleteObject(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                return redisTemplate.delete(key[0]);
            } else {
                Collection<String> strings = (Collection<String>) CollectionUtils.arrayToList(key);
                Long deletedCount = redisTemplate.delete(strings);
                return deletedCount != null && deletedCount > 0;
            }
        }
        return false;
    }

    /**
     * 缓存List数据
     *
     * @param key  键
     * @param list 值
     * @param <T>  List的类型
     * @return 缓存的对象数量
     */
    public <T> Long setListObject(String key, List<T> list) {
        Long count = redisTemplate.opsForList().rightPushAll(key, list);
        return count == null ? 0 : count;

    }

    /**
     * 获取List缓存对象
     *
     * @param key 键
     * @param <T> 类型
     * @return List 值
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getListObject(final String key) {
        try {
            return (List<T>) redisTemplate.opsForList().range(key, 0, -1);
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RedisException(e.getMessage());
        }
    }

    /**
     * 给列表右边添加元素
     *
     * @param key 列表key
     * @param str 要添加的值
     * @param <T> List的类型
     * @return 缓存的对象数量
     */
    public <T> Long addRightList(String key, String str) {
        Long count = redisTemplate.boundListOps(key).rightPush(str);
        return count == null ? 0 : count;
    }

    /**
     * 给列表左边添加元素
     *
     * @param key 列表key
     * @param str 要添加的值
     * @param <T> List的类型
     * @return 缓存的对象数量
     */
    public <T> Long addLeftList(String key, String str) {
        Long count = redisTemplate.boundListOps(key).leftPush(str);
        return count == null ? 0 : count;
    }

    /**
     * 获取右边第一个元素并删除
     *
     * @param key 列表键名
     * @param <T> List的类型
     * @return 缓存对象
     */
    public <T> Object getRightListFirstAndDelete(String key) {
        return redisTemplate.boundListOps(key).rightPop();
    }

    /**
     * 获取左边第一个元素并删除
     *
     * @param key 列表键名
     * @param <T> List的类型
     * @return 缓存对象
     */
    public <T> Object getLeftListFirstAndDelete(String key) {
        return redisTemplate.boundListOps(key).leftPop();
    }

    /**
     * 缓存Set对象
     *
     * @param key 键
     * @param set 值
     * @param <T> 类型
     * @return BoundSetOperations
     */
    @SuppressWarnings("unchecked")
    public <T> BoundSetOperations<String, T> setSetObject(String key, Set<T> set) {
        BoundSetOperations<String, T> setOperations = (BoundSetOperations<String, T>) redisTemplate.boundSetOps(key);
        for (T t : set) {
            setOperations.add(t);
        }
        return setOperations;
    }

    /**
     * 获取Set对象
     *
     * @param key 键
     * @param <T> 类型
     * @return Set集合
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> getSetObject(String key) {
        return (Set<T>) redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存Map对象
     *
     * @param key   键
     * @param value 值
     * @param <T>   类型
     * @return true=缓存成功；false=缓存失败
     */
    public <T> Boolean setMapObject(String key, Map<String, T> value) {
        try {
            if (!value.isEmpty()) {
                redisTemplate.opsForHash().putAll(key, value);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 获取缓存的Map对象
     *
     * @param key 键
     * @return Map对象
     */
    @SuppressWarnings("unchecked")
    public <T> Map<Object, T> getMapObject(String key) {
        return (Map<Object, T>) redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往缓存的Map对象中添加对象
     *
     * @param key  键
     * @param hKey hash键
     * @param data 值
     * @param <T>  类型
     * @return true=设置成功；false=设置失败
     */
    public <T> Boolean setMapValue(String key, String hKey, T data) {
        try {
            redisTemplate.opsForHash().put(key, hKey, data);
        } catch (Exception e) {
            log.info(e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * 获取Hash中的数据
     *
     * @param key  键
     * @param hKey Hash键
     * @param <T>  类型
     * @return T
     */
    public <T> T getMapValue(String key, String hKey) {
        HashOperations<String, Object, T> operations = redisTemplate.opsForHash();
        return operations.get(key, hKey);
    }

    /**
     * 删除Hash中的数据
     *
     * @param key  键
     * @param hKey Hash键
     * @return true=删除成功；false=删除失败
     */
    public Boolean deleteMapValue(String key, String hKey) {
        return redisTemplate.opsForHash().delete(key, hKey) > 0;
    }

    /**
     * 递增
     *
     * @param key   键
     * @param delta 数
     * @return Long
     */
    public Long incr(String key, Long delta) {
        if (delta < 0) {
            throw new RedisException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);

    }

    /**
     * 递减
     *
     * @param key   键
     * @param delta 数
     * @return Long
     */
    public Long decr(String key, Long delta) {
        if (delta < 0) {
            throw new RedisException("递减数必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }
}
