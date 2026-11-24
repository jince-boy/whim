package com.whim.sse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Jince
 * @date 2026/11/20
 * @description 节点间传递的事件，用户 ID 为空表示广播，数据已经按应用的 JSON 规则序列化。
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class SseEvent {
    /** 仅在服务节点间传递，用于唤醒目标用户的连接校验。 */
    static final String CHECK_SESSION = "checkSessionValidity";

    private Long userId;
    private String name;
    private String jsonData;
}
