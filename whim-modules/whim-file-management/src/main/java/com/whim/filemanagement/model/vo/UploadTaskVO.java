package com.whim.filemanagement.model.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 面向当前用户的上传状态，不暴露存储密钥、内部对象路径或原生上传会话。
 */
@Data
public class UploadTaskVO {
    private String id;
    private String fileName;
    private Long fileSize;
    private String contentType;
    private String fingerprint;
    private Long partSize;
    private Integer partCount;
    private String status;
    private Boolean directUpload;
    private LocalDateTime expiresAt;
    private LocalDateTime createTime;
    /** 详情接口返回经过存储端核对的分片编号，列表接口为空列表。 */
    private List<Integer> uploadedParts;
}

