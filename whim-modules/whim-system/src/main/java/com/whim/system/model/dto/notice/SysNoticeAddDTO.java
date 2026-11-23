package com.whim.system.model.dto.notice;

import com.whim.system.model.entity.SysNotice;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/21
 * @description 新增通知公告参数。
 */
@Data
@AutoMapper(target = SysNotice.class, reverseConvertGenerate = false)
public class SysNoticeAddDTO {

    @NotBlank(message = "公告标题不能为空")
    @Size(max = 100, message = "公告标题不能超过100个字符")
    private String title;
    @NotNull(message = "请选择公告类型")
    @Min(value = 1, message = "公告类型无效")
    @Max(value = 2, message = "公告类型无效")
    private Integer noticeType;
    @Size(max = 10000, message = "公告正文不能超过10000个字符")
    private String content;
    /** 富文本优先，服务端根据清洗结果生成纯文本。 */
    @Size(max = 262144, message = "公告排版内容过大，请简化格式")
    private String contentHtml;
    /** 正常状态且到达此时间后读者可见。 */
    private LocalDateTime publishTime;
    @NotNull(message = "请选择公告状态")
    @Min(value = 0, message = "公告状态无效")
    @Max(value = 1, message = "公告状态无效")
    private Integer status;
}
