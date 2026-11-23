package com.whim.system.model.dto.notice;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 按消息类型分页读取个人收件箱。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticeInboxQueryDTO extends PageQueryDTO {

    @Min(value = 1, message = "消息类型无效")
    @Max(value = 2, message = "消息类型无效")
    private Integer noticeType;
}
