package com.whim.file.adapter.wrapper;

import org.apache.tika.Tika;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @author jince
 * date: 2025/3/12 19:38
 * description: 字符串文件包装类
 */
public class StringWrapper extends BaseFileWrapper<String> {
    public StringWrapper(String file, Tika tika) {
        super(file, tika);
    }

    @Override
    public String getFileName() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Long getFileSize() {
        return (long) file.getBytes(StandardCharsets.UTF_8).length;
    }

    @Override
    public String getContentType() {
        return tika.detect(file.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public InputStream getInputStream() {
        if (inputStream == null) {
            inputStream = new BufferedInputStream(new ByteArrayInputStream(file.getBytes(StandardCharsets.UTF_8)));
        }
        return inputStream;
    }

    @Override
    public void close() throws Exception {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
