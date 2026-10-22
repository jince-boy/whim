package com.whim.file.support;

import io.minio.ListPartsArgs;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 补足 MinIO 9.0.3 ListPartsArgs.Builder 缺失的 object 参数，无反射调用。
 */
public final class MinioListPartsArguments extends ListPartsArgs {
    private final String sessionId;
    private final Integer marker;

    /** 固定桶、对象与分片分页游标。 */
    public MinioListPartsArguments(String bucket, String key, String uploadId, Integer marker) {
        this.bucketName = bucket;
        this.objectName = key;
        this.sessionId = uploadId;
        this.marker = marker;
    }

    /** 返回存储会话标识。 */
    @Override
    public String uploadId() {
        return sessionId;
    }

    /** 单页最多读取一千个分片。 */
    @Override
    public Integer maxParts() {
        return 1000;
    }

    /** 返回上页最后的分片编号。 */
    @Override
    public Integer partNumberMarker() {
        return marker;
    }
}

