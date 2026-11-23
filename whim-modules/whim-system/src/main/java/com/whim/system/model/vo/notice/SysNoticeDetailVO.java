package com.whim.system.model.vo.notice;

import com.whim.system.model.entity.SysNotice;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 公告正文及编辑版本。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysNotice.class, convertGenerate = false)
public class SysNoticeDetailVO extends SysNoticeVO {

    private String content;
    private String contentHtml;
    private Integer version;
}
