package com.whim.file.wrapper;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

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
    public String getDefaultFileName() {
        return UUID.randomUUID() + ".bin";
    }

    @Override
    public String getDefaultContentType() {
        return "application/octet-stream";
    }

    @Override
    public InputStream getInputStream() {
        if (inputStream == null) {
            inputStream = new BufferedInputStream(new ByteArrayInputStream(file));
        }
        return inputStream;
    }
}
