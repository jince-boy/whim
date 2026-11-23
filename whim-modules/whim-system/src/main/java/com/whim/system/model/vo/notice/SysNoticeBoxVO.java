package com.whim.system.model.vo.notice;

import java.util.List;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 顶部铃铛中的未读总数与最近五条公告。
 */
@Data
public class SysNoticeBoxVO {

    private final long unreadCount;
    private final List<SysNoticeInboxItemVO> items;
    /** 距下次发布的毫秒数，客户端据此刷新跨分类的未读角标。 */
    private final Long refreshAfterMillis;
}
