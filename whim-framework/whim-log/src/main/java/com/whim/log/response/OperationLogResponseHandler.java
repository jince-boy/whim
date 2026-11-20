package com.whim.log.response;

import com.whim.log.event.OperLogEvent;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 由应用按自身返回值约定补充操作日志。
 */
@FunctionalInterface
public interface OperationLogResponseHandler {
    /**
     * 为识别的返回值补充操作结果和可公开的响应摘要。
     *
     * @param response 控制器或异常处理器返回的非空对象
     * @param event 当前操作日志快照构建器
     */
    void recordResponse(Object response, OperLogEvent.OperLogEventBuilder event);

    /**
     * 由应用在异常转换为公开响应后完成审计，没有可等待的响应时由切面立即投递。
     *
     * @param complete 接收最终响应并投递事件的回调，未取得响应时传入空值
     * @return 应用是否已接管本次失败事件的投递
     */
    default boolean deferFailureResponse(Consumer<@Nullable Object> complete) {
        return false;
    }
}
