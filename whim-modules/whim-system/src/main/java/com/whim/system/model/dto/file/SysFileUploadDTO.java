package com.whim.system.model.dto.file;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 文件上传参数，业务入口传入上传途径，存储配置由系统默认上传设置决定。
 */
@Getter
@Setter
public class SysFileUploadDTO {
    @NotNull(message = "上传文件不能为空")
    private MultipartFile file;
    @NotBlank(message = "上传途径不能为空")
    @Size(max = 64, message = "上传途径不能超过64个字符")
    private String uploadSource = "fileManagement";
}
