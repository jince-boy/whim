package com.whim.system.model.dto.file;

import com.whim.system.model.entity.SysFileStorage;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 新增存储配置，只接收可修改字段。
 */
@Getter
@Setter
@AutoMapper(target = SysFileStorage.class, reverseConvertGenerate = false)
public class SysFileStorageAddDTO {
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 64, message = "配置名称不能超过64个字符")
    private String name;
    @NotBlank(message = "存储类型不能为空")
    @Pattern(regexp = "local|minio|aliyunOss", message = "不支持的存储类型")
    private String platform;
    @Size(max = 512, message = "目录不能超过512个字符")
    private String basePath = "";
    @Size(max = 512, message = "服务地址不能超过512个字符")
    private String endpoint = "";
    @Size(max = 128, message = "桶名称不能超过128个字符")
    private String bucket = "";
    @Size(max = 64, message = "地域不能超过64个字符")
    private String region = "";
    @Size(max = 256, message = "访问标识不能超过256个字符")
    private String accessKey = "";
    /** 修改时留空保留原密钥。 */
    @Size(max = 1024, message = "密钥不能超过1024个字符")
    private String secretKey;
}
