package com.whim.system.listener;

import com.whim.sse.SseEventPublisher;
import com.whim.system.event.NoticeChangedEvent;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 将已提交的公告变更发送给对应的在线用户。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(SseEventPublisher.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnBooleanProperty(prefix = "whim.sse", name = "enabled", matchIfMissing = true)
public class NoticeEventListener {
    private final SseEventPublisher publisher;

    /**
     * 在数据库提交后提醒客户端重新获取公告。
     *
     * @param event 已提交的公告变更
     */
    @TransactionalEventListener
    public void notifyNoticeChanged(NoticeChangedEvent event) {
        // 仅公开通知公告可以广播；所有推送都只携带刷新信号。
        if (event.getRecipientIds() == null) {
            try {
                publisher.broadcast(event.getEventName(), Map.of());
            } catch (RuntimeException exception) {
                log.warn("公告已提交，实时提醒广播失败", exception);
            }
            return;
        }
        for (Long userId : event.getRecipientIds()) {
            try {
                publisher.sendToUser(userId, event.getEventName(), Map.of());
            } catch (RuntimeException exception) {
                // 单个账号推送失败不影响其他收件人，重连或打开铃铛时可重新读取已提交的消息。
                log.warn("消息已提交，定向提醒发送失败，用户ID={}", userId, exception);
            }
        }
    }
}
