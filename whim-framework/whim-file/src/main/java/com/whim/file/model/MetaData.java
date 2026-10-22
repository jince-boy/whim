package com.whim.file.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 文件元数据；platform、platformConfigName、objectKey 共同定位文件。
 */
@Getter
@Setter
public class MetaData {
    private String platform;
    private String platformConfigName;
    /** 相对配置根目录或对象前缀的完整 key，不包含服务器绝对路径。 */
    private String objectKey;
    private String fileName;
    /** 相对目录，不包含文件名。 */
    private String storagePath;
    private Long fileSize;
    private String contentType;
}
