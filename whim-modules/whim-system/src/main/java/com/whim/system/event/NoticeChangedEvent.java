package com.whim.system.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 公告库变动或账号已读状态变动，成功提交后通知在线客户端。
 */
@Getter
@RequiredArgsConstructor
public class NoticeChangedEvent {
    /** 已读状态仅发送给当前用户；为空时通知全员刷新可见公告。 */
    private final Long userId;
}
