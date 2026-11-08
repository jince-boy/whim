package com.whim.system.model.dto.file;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 文件上传参数，存储配置由系统默认上传设置决定。
 */
@Getter
@Setter
public class SysFileUploadDTO {
    @NotNull(message = "上传文件不能为空")
    private MultipartFile file;
}
