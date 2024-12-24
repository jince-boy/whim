package com.whim.file.wrapper;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author Jince
 * date: 2024/11/15 22:36
 * description: 文件包装器基类
 */
@Getter
public abstract class BaseFileWrapper<T> implements IFileWrapper {
    protected T file;
    protected String fileName;
    protected Long fileSize;
    protected String fileContentType;
    protected String fileExtension;
    protected InputStream inputStream;

    protected BaseFileWrapper(T file) {
        this.file = Objects.requireNonNull(file, "文件不能为空");
        this.fileName = setFileName(file);
        this.fileSize = setFileSize(file);
        this.fileContentType = setFileContentType(file);
        this.fileExtension = setFileExtension(file);
        try {
            this.inputStream = setInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException("无法获取文件输入流", e);
        }
    }

    // 内部用来初始化的 set 方法，不暴露给外部
    protected abstract String setFileName(T file);

    protected abstract Long setFileSize(T file);

    protected abstract String setFileContentType(T file);

    protected abstract String setFileExtension(T file);

    protected abstract InputStream setInputStream(T file) throws IOException;

    // 允许外部修改属性的 set 方法
    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }

    @Override
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
