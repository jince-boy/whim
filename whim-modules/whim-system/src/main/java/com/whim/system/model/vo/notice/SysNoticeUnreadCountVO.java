package com.whim.system.model.vo.notice;

import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/24
 * @description 当前账号在一个消息分类中的未读条数。
 */
@Data
public class SysNoticeUnreadCountVO {

    private int noticeType;
    private long unreadCount;
}
