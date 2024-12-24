package com.whim.file.wrapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Jince
 * date: 2024/11/15 21:45
 * description:
 */
public interface IFileWrapper {
    /**
     * 设置文件名称
     */
    void setFileName(String fileName);

    /**
     * 设置文件大小
     */
    void setFileSize(Long fileSize);

    /**
     * 设置文件内容类型
     */
    void setFileContentType(String fileContentType);

    /**
     * 设置文件扩展名
     */
    void setFileExtension(String fileExtension);

    /**
     * 设置文件输入流
     */
    void setInputStream(InputStream inputStream);

    /**
     * 获取文件名称
     *
     * @return 文件名称
     */
    String getFileName();

    /**
     * 获取文件大小
     *
     * @return 文件大小
     */
    Long getFileSize();

    /**
     * 获取文件类型
     *
     * @return 文件类型
     */
    String getFileContentType();

    /**
     * 获取文件扩展名
     *
     * @return 文件扩展名
     */
    String getFileExtension();

    /**
     * 获取文件流
     *
     * @return 文件流
     */
    InputStream getInputStream() throws IOException;
}
