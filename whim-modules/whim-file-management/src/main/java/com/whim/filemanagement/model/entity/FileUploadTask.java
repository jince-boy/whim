package com.whim.filemanagement.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 上传任务兼作完成后的个人文件记录，业务归属不可更改。
 */
@Data
public class FileUploadTask {
    @TableId(type = IdType.INPUT)
    private String id;
    private Long ownerId;
    private String requestId;
    private String fileName;
    private Long fileSize;
    private String contentType;
    /** 客户端对原文件的 SHA-256，用于重选文件时匹配，不表示服务端已验证全文件摘要。 */
    private String fingerprint;
    private Long partSize;
    private Integer partCount;
    private String platform;
    private String configName;
    private String objectKey;
    private String uploadId;
    private String status;
    private Boolean cleaned;
    private LocalDateTime expiresAt;
    private LocalDateTime signatureExpiresAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

