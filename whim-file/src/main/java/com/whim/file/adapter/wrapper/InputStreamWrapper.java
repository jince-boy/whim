package com.whim.file.adapter.wrapper;

import com.whim.common.exception.FileStorageException;
import org.apache.tika.Tika;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author jince
 * date: 2025/3/11 13:32
 * description: InputStream包装器
 */
public class InputStreamWrapper extends BaseFileWrapper<InputStream> {

    public InputStreamWrapper(InputStream file, Tika tika) {
        super(file, tika);
    }

    @Override
    public String getFileName() {
        return UUID.randomUUID().toString();
    }

    @Override
    public Long getFileSize() {
        return -1L;
    }

    @Override
    public String getContentType() {
        try {
            return tika.detect(inputStream);
        } catch (IOException e) {
            throw new FileStorageException("无法获取InputStream的Content-Type");
        }
    }

    @Override
    public InputStream getInputStream() {
        if (inputStream == null) {
            inputStream = new BufferedInputStream(file);
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
