package com.whim.filemanagement.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 初始化上传参数；存储平台、路径、分片大小均由服务端选择。
 */
@Data
public class AddUploadTaskDTO {
    @NotBlank(message = "请求标识不能为空")
    @Pattern(regexp = "[a-f0-9]{32}", message = "请求标识必须是32位小写十六进制字符")
    private String requestId;
    @NotBlank(message = "文件名不能为空")
    @Size(max = 200, message = "文件名不能超过200个字符")
    private String fileName;
    @NotNull(message = "文件大小不能为空")
    @Min(value = 1, message = "文件不能为空")
    private Long fileSize;
    @NotBlank(message = "文件内容类型不能为空")
    @Size(max = 150, message = "文件内容类型过长")
    private String contentType;
    @NotBlank(message = "文件指纹不能为空")
    @Pattern(regexp = "[a-f0-9]{64}", message = "文件指纹必须为SHA-256")
    private String fingerprint;
}

