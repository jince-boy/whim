package com.whim.file.adapter.wrapper;

import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * @author jince
 * date: 2025/3/11 13:32
 * description: InputStream包装器
 */
public class InputStreamWrapper extends BaseFileWrapper<InputStream> {

    public InputStreamWrapper(InputStream file) {
        super(file);
    }

    @Override
    public String getExtension() {
        return "";
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
