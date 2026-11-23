package com.whim.system.model.dto.notice;

import com.whim.mybatisplus.model.dto.PageQueryDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 管理端公告分页筛选条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticeQueryDTO extends PageQueryDTO {

    @Size(max = 100, message = "标题筛选不能超过100个字符")
    private String title;
    @Min(value = 1, message = "公告类型无效")
    @Max(value = 2, message = "公告类型无效")
    private Integer noticeType;
    @Min(value = 0, message = "公告状态无效")
    @Max(value = 1, message = "公告状态无效")
    private Integer status;
}
