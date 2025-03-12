package com.whim.file.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jince
 * date: 2025/3/7 13:44
 * description: 文件信息VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfoVO {
    private String fileName;
    private String storagePath;
    private String platform;
    private String fileSize;
    private String contentType;
}
