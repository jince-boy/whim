package com.whim.sse;

import com.whim.core.auth.constants.AuthUserType;
import com.whim.core.auth.event.UserSessionRevokedEvent;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.redisson.api.RTopic;
import org.springframework.context.event.EventListener;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 发布仅面向当前在线系统用户的实时事件，事务业务应在提交成功后调用。
 */
@RequiredArgsConstructor
@Slf4j
public class SseEventPublisher {

    private static final Pattern EVENT_NAME = Pattern.compile("[a-zA-Z][a-zA-Z0-9._-]{0,63}");

    private final SseConnectionRegistry registry;
    private final JsonMapper jsonMapper;
    private final @Nullable RTopic topic;
    private Integer listenerId;

    /**
     * 订阅集群事件并投递给本机连接。
     */
    @PostConstruct
    void subscribe() {
        if (topic != null) {
            listenerId = topic.addListener(String.class, (channel, json) ->
                    registry.deliver(jsonMapper.readValue(json, SseEvent.class)));
        }
    }

    /**
     * 移除本发布器注册的 Redis 监听器。
     */
    @PreDestroy
    void unsubscribe() {
        if (topic != null && listenerId != null) {
            topic.removeListener(listenerId);
        }
    }

    /**
     * 在认证层完成撤销后唤醒本地或集群上的对应账号订阅。
     *
     * @param event 已完成撤销的账号
     */
    @EventListener
    public void checkUserSessions(UserSessionRevokedEvent event) {
        if (AuthUserType.SYSTEM.equals(event.getLoginType())) {
            try {
                sendToUser(event.getUserId(), SseEvent.CHECK_SESSION, Map.of());
            } catch (RuntimeException exception) {
                // 撤销已经生效，推送失败仍由后续投递、心跳及重连鉴权发现。
                log.warn("会话撤销后的实时通知失败，用户ID={}", event.getUserId(), exception);
            }
        }
    }

    /**
     * 将事件发送到指定系统用户的全部在线连接。
     *
     * @param userId 接收用户 ID
     * @param eventName SSE 事件名称
     * @param data 可序列化为 JSON 的事件数据
     */
    public void sendToUser(Long userId, String eventName, Object data) {
        publish(Objects.requireNonNull(userId, "接收用户 ID 不能为空"), eventName, data);
    }

    /**
     * 将事件发送到全部在线系统用户的连接。
     *
     * @param eventName SSE 事件名称
     * @param data 可序列化为 JSON 的事件数据
     */
    public void broadcast(String eventName, Object data) {
        publish(null, eventName, data);
    }

    /**
     * 按部署方式投递已序列化的事件。
     *
     * @param userId 接收用户 ID，广播时为空
     * @param eventName SSE 事件名称
     * @param data 事件数据
     */
    private void publish(@Nullable Long userId, String eventName, Object data) {
        if (eventName == null || !EVENT_NAME.matcher(eventName).matches()) {
            throw new IllegalArgumentException("事件名须以字母开头，限 64 位字母、数字、点、下划线或连字符");
        }
        SseEvent event = new SseEvent(userId, eventName, jsonMapper.writeValueAsString(data));
        if (topic == null) {
            registry.deliver(event);
        } else {
            // 发布节点也会收到同一主题的回调，本地再次投递会造成重复事件。
            topic.publish(jsonMapper.writeValueAsString(event));
        }
    }
}
