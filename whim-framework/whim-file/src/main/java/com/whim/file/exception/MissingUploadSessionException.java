package com.whim.file.exception;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 上传会话已明确丢失；业务层据此区分不可恢复状态与临时网络故障。
 */
public final class MissingUploadSessionException extends RuntimeException {
    /** 创建明确的会话丢失异常，保留底层原因。 */
    public MissingUploadSessionException(Throwable cause) {
        super("上传分片会话已不存在，请重新上传文件", cause);
    }
}

