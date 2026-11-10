package com.whim.system.model.dto.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jince
 * @date 2026/11/10
 * @description 创建上传任务的文件信息，重复请求沿用同一请求标识。
 */
@Getter
@Setter
public class SysFileUploadTaskAddDTO {
    @NotBlank(message = "上传请求标识不能为空")
    @Pattern(regexp = "[a-f0-9]{32}", message = "上传请求标识格式无效")
    private String requestId;
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名不能超过255个字符")
    private String originalName;
    @NotNull(message = "文件大小不能为空")
    @Positive(message = "不能上传空文件")
    private Long fileSize;
    @NotBlank(message = "文件类型不能为空")
    @Size(max = 255, message = "文件类型不能超过255个字符")
    private String contentType;
    @NotBlank(message = "上传途径不能为空")
    @Size(max = 64, message = "上传途径不能超过64个字符")
    private String uploadSource = "fileManagement";
}
