package com.whim.file.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * @author jince
 * date: 2025/3/7 13:44
 * description: 文件信息
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class FileInfo {
    private String fileName;
    private String fileSize;
    private String storagePath;
    private String platform;
    private String platformConfigName;
    private String contentType;
    private InputStream inputStream;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;
}
