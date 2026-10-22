package com.whim.filemanagement.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 签名或本地上传前固定的分片内容约束，不使用客户端 ETag 作为事实来源。
 */
@Data
public class FileUploadPart {
    @TableId(type = IdType.INPUT)
    private String id;
    private String taskId;
    private Integer partNumber;
    private Long partSize;
    private String contentMd5;
    private String contentSha256;
}

