package com.whim.system.model.vo.notice;

import com.whim.system.model.entity.SysNotice;
import io.github.linpeilie.annotations.AutoMapper;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 管理端公告摘要。
 */
@Data
@AutoMapper(target = SysNotice.class, convertGenerate = false)
public class SysNoticeVO {

    private Long id;
    private String title;
    private Integer noticeType;
    private Integer status;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
}
