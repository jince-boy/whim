package com.whim.file.adapter.wrapper;

import com.whim.common.exception.FileStorageException;
import org.apache.tika.Tika;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author jince
 * date: 2025/3/12 19:33
 * description: URL文件包装器
 */
public class URLWrapper extends BaseFileWrapper<URL> {
    public URLWrapper(URL file, Tika tika) {
        super(file, tika);
    }

    @Override
    public String getFileName() {
        String path = file.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    @Override
    public Long getFileSize() {
        try {
            URLConnection connection = file.openConnection();
            return connection.getContentLengthLong();
        } catch (IOException e) {
            throw new FileStorageException("无法获取URL文件大小", e);
        }
    }

    @Override
    public String getContentType() {
        try {
            URLConnection connection = file.openConnection();
            return connection.getContentType();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getInputStream() {
        if (inputStream == null) {
            try {
                inputStream = new BufferedInputStream(file.openStream());
            } catch (IOException e) {
                throw new FileStorageException("无法打开URL的输入流", e);
            }
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
