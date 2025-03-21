package com.whim.file.adapter.wrapper;

import com.whim.common.exception.FileStorageException;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author jince
 * date: 2025/3/12 19:40
 * description: File文件包装器
 */
public class FileWrapper extends BaseFileWrapper<File> {
    public FileWrapper(File file) {
        super(file);
    }

    @Override
    public String getExtension() {
        return FilenameUtils.getExtension(file.getName());
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

    @Override
    public void close() throws Exception {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
