package com.whim.file.adapter.wrapper;

import com.whim.common.exception.FileStorageException;
import org.apache.tika.Tika;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jince
 * date: 2025/3/12 19:40
 * description: File文件包装器
 */
public class FileWrapper extends BaseFileWrapper<File> {
    public FileWrapper(File file, Tika tika) {
        super(file, tika);
    }

    @Override
    public String getFileName() {
        return file.getName();
    }

    @Override
    public Long getFileSize() {
        return file.length();
    }

    @Override
    public String getContentType() {
        try {
            return tika.detect(file);
        } catch (IOException e) {
            throw new FileStorageException("无法获取File的Content-Type", e);
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

    @Override
    public void close() throws Exception {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
