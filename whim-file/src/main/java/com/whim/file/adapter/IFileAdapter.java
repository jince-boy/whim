package com.whim.file.adapter;

import com.whim.file.wrapper.IFileWrapper;

/**
 * @author Jince
 * date: 2024/11/15 22:27
 * description: 文件适配器接口
 */
public interface IFileAdapter {
    /**
     * 是否支持该文件类型
     *
     * @param file 文件
     * @return true 支持，false 不支持
     */
    boolean isSupport(Object file);

    /**
     * 获取文件包装器
     *
     * @param file 文件
     * @return 文件包装器
     */
    IFileWrapper getFileWrapper(Object file);
}
