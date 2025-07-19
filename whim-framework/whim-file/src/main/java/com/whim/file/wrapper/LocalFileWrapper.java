package com.whim.file.wrapper;

import com.whim.core.exception.FileStorageException;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;

/**
 * @author jince
 * @date 2025/3/12 19:40
 * @description File文件包装器
 */
public class LocalFileWrapper extends BaseFileWrapper<File> {
    public LocalFileWrapper(File file) {
        super(file);
    }

    @Override
    public String getDefaultFileName() {
        return UUID.randomUUID() + "." + FilenameUtils.getExtension(file.getName());
    }

    @Override
    public String getDefaultContentType() {
        try {
            return Files.probeContentType(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream getInputStream() {
        if (inputStream == null) {
            try {
                inputStream = new BufferedInputStream(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                throw new FileStorageException("文件未找到", e);
            }
        }
        return inputStream;
    }
}
