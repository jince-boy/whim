package com.whim.system.model.dto.file;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Jince
 * @date 2026/11/10
 * @description 当前任务的单个分片，序号及实际大小均由服务端复核。
 */
@Getter
@Setter
public class SysFileUploadPartDTO {
    @NotNull(message = "上传任务ID不能为空")
    private Long id;
    @NotNull(message = "分片序号不能为空")
    @Min(value = 1, message = "分片序号必须从1开始")
    private Integer partNumber;
    @NotNull(message = "分片文件不能为空")
    private MultipartFile file;
}
