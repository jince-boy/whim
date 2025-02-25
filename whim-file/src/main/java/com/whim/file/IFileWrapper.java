package com.whim.file;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jince
 * date: 2025/2/17 21:22
 * description: 文件包装器接口
 */
public interface IFileWrapper extends AutoCloseable {
    String getFileName();

    Long getFileSize() throws IOException;

    String getContentType();

    InputStream getInputStream() throws IOException;

    @Override
    void close() throws Exception;
}
