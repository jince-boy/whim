package com.whim.file.adapter.wrapper;

import org.apache.tika.Tika;

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
    public ByteArrayWrapper(byte[] file, Tika tika) {
        super(file, tika);
    }

    @Override
    public String getFileName() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Long getFileSize() {
        return (long) file.length;
    }

    @Override
    public String getContentType() {
        return tika.detect(file);
    }

    @Override
    public InputStream getInputStream() {
        if (inputStream == null) {
            inputStream = new BufferedInputStream(new ByteArrayInputStream(file));
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
