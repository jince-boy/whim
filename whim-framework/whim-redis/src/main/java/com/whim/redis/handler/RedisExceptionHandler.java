package com.whim.redis.handler;

import com.baomidou.lock.exception.LockFailureException;
import com.whim.core.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author jince
 * date: 2025/6/23 17:29
 * description: 统一处理Redis异常
 */
@Slf4j
@RestControllerAdvice
public class RedisExceptionHandler {
    /**
     * 分布式锁Lock4j异常
     */
    @ExceptionHandler(LockFailureException.class)
    public Result<Void> handleLockFailureException(LockFailureException e) {
        log.error("获取锁失败了,发生Lock4j异常.", e);
        return Result.error("业务处理中，请稍后再试...");
    }
}
