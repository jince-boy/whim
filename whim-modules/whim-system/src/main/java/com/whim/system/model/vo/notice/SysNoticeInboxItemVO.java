package com.whim.system.model.vo.notice;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 当前账号可见的公告摘要与阅读状态。
 */
@Data
public class SysNoticeInboxItemVO {

    private Long id;
    private String title;
    private Integer noticeType;
    private LocalDateTime publishTime;
    private boolean readFlag;
}
