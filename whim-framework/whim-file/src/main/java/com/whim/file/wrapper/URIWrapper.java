package com.whim.file.wrapper;

import com.whim.core.exception.FileStorageException;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

/**
 * @author jince
 * @date 2025/3/13 15:54
 * @description URI文件
 */
public class URIWrapper extends BaseFileWrapper<URI> {
    public URIWrapper(URI file) {
        super(file);
    }

    @Override
    public String getDefaultFileName() {
        String path = file.getPath();
        return UUID.randomUUID() + "." + FilenameUtils.getExtension(path.substring(path.lastIndexOf('/') + 1));
    }

    @Override
    public String getDefaultContentType() {
        try {
            URLConnection connection = file.toURL().openConnection();
            return connection.getContentType();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getInputStream() {
        if (inputStream == null) {
            try {
                URL url = file.toURL();
                inputStream = new BufferedInputStream(url.openStream());
            } catch (IOException e) {
                throw new FileStorageException("无法打开URI的输入流", e);
            }
        }
        return inputStream;
    }
}
