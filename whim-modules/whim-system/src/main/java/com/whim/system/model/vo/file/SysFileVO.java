package com.whim.system.model.vo.file;

import com.whim.system.model.entity.SysFile;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 文件管理响应，不包含存储密钥。
 */
@Data
@AutoMapper(target = SysFile.class, convertGenerate = false)
public class SysFileVO {
    private Long id;
    private Long storageId;
    private String storageName;
    private String platform;
    private String originalName;
    private String objectKey;
    private Long fileSize;
    private String contentType;
    private LocalDateTime createTime;
}
