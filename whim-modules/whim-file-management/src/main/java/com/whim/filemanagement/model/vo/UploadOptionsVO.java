package com.whim.filemanagement.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 前端上传约束，文件大小以字节表示。
 */
@Getter
@AllArgsConstructor
public class UploadOptionsVO {
    private final long maximumFileSize;
    private final long partSize;
    private final int concurrency;
}

