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
    }

    /**
     * 设置文件名称
     *
     * @return 文件名称
     */
    protected abstract String setFileName();

    /**
     * 设置文件大小
     *
     * @return 文件大小
     */
    protected abstract Long setFileSize();

    /**
     * 设置文件类型
     *
     * @return 文件类型
     */
    protected abstract String setFileContentType();

    /**
     * 设置文件扩展名
     *
     * @return 文件扩展名
     */
    protected abstract String setFileExtension();

    /**
     * 设置文件输入流
     *
     * @return 文件输入流
     * @throws IOException IO异常
     */
    protected abstract InputStream setInputStream() throws IOException;
}
