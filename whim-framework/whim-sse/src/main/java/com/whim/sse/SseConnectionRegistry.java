package com.whim.sse;

import com.whim.core.exception.TooManyRequestsException;
import com.whim.sse.config.SseProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 管理本机系统用户订阅，各连接独立排队并顺序写出事件。
 */
@Slf4j
@RequiredArgsConstructor
public class SseConnectionRegistry implements AutoCloseable {

    private static final MediaType UTF8_TEXT = new MediaType("text", "plain", StandardCharsets.UTF_8);

    private final SseProperties properties;
    private final Map<Long, Set<Connection>> connections = new HashMap<>();
    private boolean closed;

    /**
     * 为当前用户新增一个独立订阅。
     *
     * @param userId 经过认证的系统用户 ID
     * @param sessionValid 可在后台线程调用的原会话有效性检查器
     * @return 当前 HTTP 请求的 SSE 响应流
     */
    public synchronized SseEmitter subscribe(Long userId, BooleanSupplier sessionValid) {
        if (closed) {
            throw new IllegalStateException("事件流服务正在关闭");
        }
        Set<Connection> subscriptions = connections.computeIfAbsent(userId, key -> new HashSet<>());
        if (subscriptions.size() >= properties.getMaxConnectionsPerUser()) {
            throw new TooManyRequestsException("当前用户的实时连接数已达上限，请关闭其他页面后重试");
        }
        // 同一会话的标签页分别注册，旧请求的关闭回调只影响该次订阅。
        Connection connection = new Connection(userId, sessionValid);
        connection.emitter.onCompletion(connection::stop);
        connection.emitter.onError(error -> connection.stop());
        connection.emitter.onTimeout(connection::stop);
        subscriptions.add(connection);
        connection.writer.start();
        return connection.emitter;
    }

    /**
     * 将事件加入目标连接的有界队列。
     *
     * @param event 要投递的事件
     */
    synchronized void deliver(SseEvent event) {
        if (event.getUserId() == null) {
            connections.values().forEach(group -> group.forEach(connection -> connection.enqueue(event)));
        } else {
            Set<Connection> group = connections.get(event.getUserId());
            if (group != null) {
                group.forEach(connection -> connection.enqueue(event));
            }
        }
    }

    /**
     * 移除已结束的订阅并回收空的用户连接组。
     *
     * @param connection 已结束的订阅
     */
    private synchronized void remove(Connection connection) {
        Set<Connection> group = connections.get(connection.userId);
        if (group != null && group.remove(connection) && group.isEmpty()) {
            connections.remove(connection.userId);
        }
    }

    /**
     * 停止接入新订阅并唤醒所有写线程结束响应。
     */
    @Override
    @EventListener(ContextClosedEvent.class)
    public synchronized void close() {
        closed = true;
        // 在 Servlet 容器等待优雅停机前结束长连接。
        connections.values().forEach(group -> group.forEach(Connection::stop));
        connections.clear();
    }

    /**
     * @author Jince
     * @date 2026/11/20
     * @description 单次 HTTP 订阅的写线程与有界事件队列。
     */
    @RequiredArgsConstructor
    private final class Connection {

        private final Long userId;
        private final BooleanSupplier sessionValid;
        private final SseEmitter emitter = new SseEmitter(0L);
        private final BlockingQueue<SseEvent> pending = new ArrayBlockingQueue<>(properties.getQueueCapacity());
        private final Thread writer = Thread.ofVirtual().name("whim-sse-writer")
                .inheritInheritableThreadLocals(false).unstarted(this::write);
        private volatile boolean stopped;

        /**
         * 尝试入队，慢客户端积压超限时结束连接。
         *
         * @param event 要写入的事件
         */
        private void enqueue(SseEvent event) {
            // 避免慢连接阻塞其他用户或 Redis 监听线程，客户端重连后重新查询业务状态。
            if (!pending.offer(event)) {
                stop();
            }
        }

        /**
         * 终止当前订阅并唤醒等待事件的写线程。
         */
        private void stop() {
            stopped = true;
            writer.interrupt();
        }

        /**
         * 依序写入事件或空闲心跳，直至会话失效、客户端断线或服务关闭。
         */
        private void write() {
            boolean writeFailed = false;
            try {
                emitter.send(SseEmitter.event().comment("connected"));
                while (!stopped) {
                    SseEvent event = pending.poll(properties.getHeartbeatSeconds(), TimeUnit.SECONDS);
                    // 长连接只在建立时经过 MVC 认证；后续投递也必须服从退出、踢下线和过期。
                    if (!sessionValid.getAsBoolean() || stopped) {
                        break;
                    }
                    if (event == null) {
                        emitter.send(SseEmitter.event().comment("heartbeat"));
                    } else {
                        emitter.send(SseEmitter.event().name(event.getName()).data(event.getJsonData(), UTF8_TEXT));
                    }
                }
            } catch (InterruptedException ignored) {
                // 容器结束请求、队列溢出和应用停机都会唤醒等待中的连接。
                Thread.currentThread().interrupt();
            } catch (IOException ignored) {
                // Servlet 容器会完成写失败的请求，重复 complete 会触发二次分派。
                writeFailed = true;
            } catch (RuntimeException exception) {
                if (!stopped) {
                    log.warn("实时连接终止，用户 ID={}，异常类型={}", userId, exception.getClass().getSimpleName());
                }
            } finally {
                remove(this);
                if (!writeFailed) {
                    emitter.complete();
                }
            }
        }
    }
}
