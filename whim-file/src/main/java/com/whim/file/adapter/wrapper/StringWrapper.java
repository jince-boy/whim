package com.whim.file.adapter.wrapper;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author jince
 * date: 2025/3/12 19:38
 * description: 字符串文件包装类
 */
public class StringWrapper extends BaseFileWrapper<String> {
    public StringWrapper(String file) {
        super(file);
    }

    @Override
    public String getExtension() {
        return "";
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
