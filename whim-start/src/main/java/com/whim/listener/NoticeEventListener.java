package com.whim.listener;

import com.whim.sse.SseEventPublisher;
import com.whim.system.event.NoticeChangedEvent;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
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
        try {
            // 阅读状态只属于当前用户；公告变更可唤醒全部在线账号。
            if (event.getUserId() == null) {
                publisher.broadcast("noticeChanged", Map.of());
            } else {
                publisher.sendToUser(event.getUserId(), "noticeRead", Map.of());
            }
        } catch (RuntimeException exception) {
            // 推送失败无法回滚已提交的数据，客户端打开铃铛或重连时会重新查询。
            log.warn("公告已提交，实时提醒发送失败", exception);
        }
    }
}
