package com.whim.system.model.dto.notice;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 读取指定公告正文中引用的图片。
 */
@Data
public class SysNoticeImageQueryDTO {
    @NotNull(message = "公告ID不能为空")
    private Long noticeId;

    @NotNull(message = "图片文件ID不能为空")
    private Long fileId;
}
