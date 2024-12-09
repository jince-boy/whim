package com.whim.file.wrapper;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Jince
 * date: 2024/11/14 23:13
 * description: MultipartFile 包装器
 */
public class MultipartFileWrapper extends BaseFileWrapper<MultipartFile> {

    public MultipartFileWrapper(MultipartFile file) {
        super(file);
    }

    @Override
    protected String setFileName() {
        return "";
    }

    @Override
    protected Long setFileSize() {
        return 0L;
    }

    @Override
    protected String setFileContentType() {
        return "";
    }

    @Override
    protected String setFileExtension() {
        return "";
    }

    @Override
    protected InputStream setInputStream() throws IOException {
        return null;
    }
}
