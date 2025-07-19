package com.whim.file.wrapper;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author jince
 * @date 2025/3/11 13:32
 * @description InputStream包装器
 */
public class InputStreamWrapper extends BaseFileWrapper<InputStream> {

    public InputStreamWrapper(InputStream file) {
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
            inputStream = new BufferedInputStream(file);
        }
        return inputStream;
    }
}
