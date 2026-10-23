package com.whim.file.storage;

import com.whim.core.exception.FileStorageException;
import com.whim.file.model.PartUploadSignature;
import com.whim.file.model.StoragePart;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 可恢复分片存储能力；任务归属、状态、并发及校验清单由业务层管理。
 */
public interface MultipartFileStorage extends IFileStorage {
    /** 创建上传会话；taskId 用于本地目录的确定性定位。 */
    String initiateUpload(String key, String contentType, String taskId);

    /** 查询存储端全部已提交分片，必须处理分页；本地读取持久化摘要，会话丢失时抛出异常。 */
    List<StoragePart> getUploadedParts(String key, String uploadId);

    /** 是否通过浏览器直接上传到对象存储。 */
    default boolean supportsDirectUpload() {
        return true;
    }

    /** 生成绑定会话、序号、长度及提供商校验摘要的短期上传签名。 */
    default PartUploadSignature getPartUploadSignature(String key, String uploadId, int partNumber,
                                                       long size, String contentMd5, String contentSha256,
                                                       Duration expiration) {
        throw new FileStorageException("当前存储不支持分片直传");
    }

    /** 写入本地分片；调用者拥有输入流并负责串行化同一任务的状态变更。 */
    default void uploadPart(String key, String uploadId, int partNumber, InputStream input,
                            long size, String contentMd5, String contentSha256) {
        throw new FileStorageException("当前存储请使用分片直传");
    }

    /** 使用服务端查询得到的完整清单合并并清理会话；本地清理失败时对象已发布，可重试 abortUpload。 */
    void completeUpload(String key, String uploadId, List<StoragePart> parts, long size, String contentType);

    /** 幂等终止会话并清理临时分片，不删除已完成的正式对象。 */
    void abortUpload(String key, String uploadId);
}

