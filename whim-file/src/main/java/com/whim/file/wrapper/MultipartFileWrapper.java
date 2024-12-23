package com.whim.file.wrapper;

import org.apache.commons.io.FilenameUtils;
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
    protected String setFileName(MultipartFile file) {
        return FilenameUtils.getBaseName(file.getOriginalFilename());
    }

    @Override
    protected Long setFileSize(MultipartFile file) {
        return file.getSize();
    }

    @Override
    protected String setFileContentType(MultipartFile file) {
        return file.getContentType();
    }

    @Override
    protected String setFileExtension(MultipartFile file) {
        return FilenameUtils.getExtension(file.getOriginalFilename());
    }

    @Override
    protected InputStream setInputStream(MultipartFile file) throws IOException {
        return file.getInputStream();
    }
}
