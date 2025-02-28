package com.whim.file.adapter.wrapper;

import com.whim.common.exception.FileStorageException;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jince
 * date: 2025/2/17 21:46
 * description: MultipartFile包装器
 */
public class MultipartFileWrapper extends BaseFileWrapper<MultipartFile> {

    public MultipartFileWrapper(MultipartFile file) {
        super(file);
    }

    @Override
    public InputStream getInputStream() {
        if (inputStream == null) {
            try {
                inputStream = new BufferedInputStream(file.getInputStream());
            } catch (IOException e) {
                throw new FileStorageException(e);
            }
        }
        return inputStream;
    }

    @Override
    public String getFileName() {
        return file.getOriginalFilename();
    }

    @Override
    public Long getFileSize() {
        return file.getSize();
    }

    @Override
    public String getContentType() {
        return file.getContentType();
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
