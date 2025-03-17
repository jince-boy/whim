package com.whim.file.adapter.wrapper;

import com.whim.common.exception.FileStorageException;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * @author jince
 * date: 2025/3/13 15:54
 * description: URI文件
 */
public class URIWrapper extends BaseFileWrapper<URI> {
    public URIWrapper(URI file) {
        super(file);
    }

    @Override
    public String getExtension() {
        return FilenameUtils.getExtension(file.getPath().substring(file.getPath().lastIndexOf("/") + 1));
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

    @Override
    public void close() throws Exception {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
