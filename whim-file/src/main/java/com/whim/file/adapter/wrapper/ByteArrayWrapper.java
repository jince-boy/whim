package com.whim.file.adapter.wrapper;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author jince
 * date: 2025/3/12 19:26
 * description: byte数组包装类
 */
public class ByteArrayWrapper extends BaseFileWrapper<byte[]> {
    public ByteArrayWrapper(byte[] file) {
        super(file);
    }

    @Override
    public InputStream getInputStream() {
        if (inputStream == null) {
            inputStream = new BufferedInputStream(new ByteArrayInputStream(file));
        }
        return inputStream;
    }

    @Override
    public String getExtension() {
        return "";
    }

    @Override
    public void close() throws Exception {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
