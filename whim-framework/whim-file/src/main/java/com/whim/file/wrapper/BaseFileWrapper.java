package com.whim.file.wrapper;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jince
 * @date 2025/2/17 21:41
 * @description 文件包装器基类
 */
@Slf4j
@RequiredArgsConstructor
@ToString
public abstract class BaseFileWrapper<T> implements IFileWrapper {
    protected final T file;
    protected InputStream inputStream;

    public abstract String getDefaultFileName();

    public abstract String getDefaultContentType();

    @Override
    public void close() throws IOException {
        log.info("文件包装器的流关闭了");
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
