package com.whim.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 已上传的文件，记录原始存储身份。
 */
@Getter
@Setter
public class SysFile extends BaseEntity {
    private Long id;
    private Long storageId;
    private String storageName;
    private String platform;
    private String uploadSource;
    private String fileName;
    private String originalName;
    private String fileSuffix;
    private String objectKey;
    private Long fileSize;
    private String contentType;
    @TableLogic
    private Long deleted;
}
