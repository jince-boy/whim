package com.whim.core.utils;

import org.slf4j.MDC;

/**
 * @author Jince
 * @date 2024/10/19 00:54
 * @description 用于在MDC（映射诊断上下文）中管理Trace ID的工具类。
 */
public class TraceIdUtils {
    public static final String TRACE_ID = "TRACE_ID";

    /**
     * 生成Trace ID
     *
     * @return Trace ID
     */
    public static String generateTraceId() {
        return IDGeneratorUtils.generateUUID();
    }

    /**
     * 设置Trace ID
     *
     * @param traceId Trace ID
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID, traceId);
    }

    /**
     * 获取Trace ID
     *
     * @return 获取当前的Trace ID，如果没有则返回null
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID);
    }

    /**
     * 从MDC中移除Trace ID
     */
    public static void clearTraceId() {
        MDC.remove(TRACE_ID);
    }

    /**
     * 管理Trace ID设置和移除的上下文管理器。
     */
    public static class TraceIdContext implements AutoCloseable {

        public TraceIdContext(String traceId) {
            setTraceId(traceId);
        }

        @Override
        public void close() throws Exception {
            clearTraceId();
        }
    }
}
