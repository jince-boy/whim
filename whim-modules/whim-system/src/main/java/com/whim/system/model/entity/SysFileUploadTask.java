package com.whim.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/11/10
 * @description 可恢复上传任务，创建人用于校验归属，固定存储位置直到完成或取消。
 */
@Getter
@Setter
public class SysFileUploadTask extends BaseEntity {
    private Long id;
    private String requestId;
    private Long storageId;
    private String objectKey;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private String uploadSource;
    private String uploadId;
    private Integer partSize;
    private Long fileId;
    private Boolean cancelled;
    private LocalDateTime expiresAt;
    @TableLogic
    private Long deleted;
}
