package com.whim.system.model.vo.file;

import com.whim.system.model.entity.SysFileStorage;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 文件管理响应，不包含存储密钥。
 */
@Data
@AutoMapper(target = SysFileStorage.class, convertGenerate = false)
public class SysFileStorageVO {
    private Long id;
    private String name;
    private String platform;
    private String basePath;
    private String endpoint;
    private String bucket;
    private String region;
    private String accessKey;
    private Integer defaultFlag;
    private LocalDateTime createTime;
}
