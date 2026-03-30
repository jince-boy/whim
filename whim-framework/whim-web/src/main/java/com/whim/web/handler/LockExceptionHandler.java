package com.whim.web.handler;

import com.baomidou.lock.exception.LockFailureException;
import com.whim.core.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Jince
 * @date 2026/03/30
 * @description Lock4j 分布式锁异常处理器。
 * 仅在 Lock4j 依赖存在于 classpath 时生效，由 {@link com.whim.web.config.WebConfig} 条件注册。
 */
@Slf4j
@RestControllerAdvice
public class LockExceptionHandler {

    /**
     * 处理分布式锁获取失败异常
     *
     * @param exception 锁获取失败异常
     * @return 统一错误响应
     */
    @ExceptionHandler(LockFailureException.class)
    public Result<Void> handleLockFailureException(LockFailureException exception) {
        log.error("分布式锁获取失败", exception);
        return Result.error("业务处理中，请稍后再试...");
    }
}
