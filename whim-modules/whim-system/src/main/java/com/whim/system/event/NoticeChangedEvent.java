package com.whim.system.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 消息变更提交后，通知对应在线客户端刷新收件箱。
 */
@Getter
@RequiredArgsConstructor
public class NoticeChangedEvent {
    public static final String NOTICE_CHANGED = "noticeChanged";
    public static final String NOTICE_READ = "noticeRead";
    public static final String SYSTEM_MESSAGE = "systemMessage";
    public static final String SYSTEM_MESSAGES_CLEARED = "systemMessagesCleared";

    /** 为空时广播公开公告变更，个人消息和已读状态只定向发送。 */
    private final Set<Long> recipientIds;
    private final String eventName;
}
