package com.whim.file.adapter;

import com.whim.file.adapter.wrapper.IFileWrapper;

/**
 * @author jince
 * date: 2025/2/17 18:04
 * description: 文件适配器
 */
public interface IFileAdapter {
    /**
     * 判断是否支持该文件
     *
     * @param file file
     * @return Boolean true支持，false不支持
     */
    boolean isSupport(Object file);

    /**
     * 获取文件包装类
     *
     * @param file file
     * @return IFileWrapper 文件包装类
     */
    IFileWrapper getFileWrapper(Object file);
}
