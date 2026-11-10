package com.whim.redis.executor;

import com.baomidou.lock.executor.RedissonLockExecutor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * @author Jince
 * @date 2026/11/10
 * @description 为Lock4j提供与同资源读锁互斥的Redisson写锁。
 */
public class RedissonWriteLockExecutor extends RedissonLockExecutor {
    private final RedissonClient redissonClient;

    /**
     * 创建使用统一Redis客户端的写锁执行器。
     *
     * @param redissonClient Redis客户端
     */
    public RedissonWriteLockExecutor(RedissonClient redissonClient) {
        super(redissonClient);
        this.redissonClient = redissonClient;
    }

    /**
     * 在指定等待时间内获取写锁，未指定租期时沿用Redisson自动续期。
     *
     * @param lockKey 锁资源键
     * @param lockValue Lock4j生成的锁标识
     * @param expire 锁租期，单位毫秒
     * @param acquireTimeout 获取锁的等待时间，单位毫秒
     * @return 已获取的写锁，等待超时返回空
     */
    @Override
    public RLock acquire(String lockKey, String lockValue, long expire, long acquireTimeout) {
        RLock lock = redissonClient.getReadWriteLock(lockKey).writeLock();
        try {
            return obtainLockInstance(lock.tryLock(acquireTimeout, expire, TimeUnit.MILLISECONDS), lock);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("获取分布式写锁时被中断", exception);
        }
    }
}
