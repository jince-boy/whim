package com.whim.filemanagement.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 分片内容约束，MD5 仅用于兼容存储端传输校验，不承担授权或秒传判定。
 */
@Data
public class UploadPartDTO {
    @NotBlank(message = "分片校验值不能为空")
    @Pattern(regexp = "[A-Za-z0-9+/]{22}==", message = "分片校验值必须是Base64编码的MD5")
    private String contentMd5;
    @NotBlank(message = "分片SHA-256不能为空")
    @Pattern(regexp = "[A-Za-z0-9+/]{43}=", message = "分片SHA-256必须使用Base64编码")
    private String contentSha256;
}

