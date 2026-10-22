package com.whim.file.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 存储端确认的分片，ETag 仅用于合并，不作为通用文件摘要。
 */
@Getter
@AllArgsConstructor
public class StoragePart {
    private final int partNumber;
    private final long size;
    private final String etag;
    private final String contentSha256;

    /** 不提供 SHA-256 的存储仍保留其原生 ETag。 */
    public StoragePart(int partNumber, long size, String etag) {
        this(partNumber, size, etag, null);
    }
}

