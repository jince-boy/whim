package com.whim.file.adapter.wrapper;

import java.io.InputStream;

/**
 * @author jince
 * date: 2025/2/17 21:22
 * description: 文件包装器接口
 */
public interface IFileWrapper extends AutoCloseable {
    String getFileName();

    Long getFileSize();

    String getContentType();

    InputStream getInputStream();

    @Override
    void close() throws Exception;
}
