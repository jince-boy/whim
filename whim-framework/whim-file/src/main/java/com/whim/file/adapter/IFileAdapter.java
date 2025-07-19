package com.whim.file.adapter;

import com.whim.file.wrapper.IFileWrapper;

/**
 * @author jince
 * @date 2025/2/17 18:04
 * @description 文件适配器
 */
public interface IFileAdapter {
    /**
     * 判断是否支持该文件
     *
     * @param file 待判断的文件对象
     * @return Boolean true表示支持，false表示不支持
     */
    boolean isSupport(Object file);

    /**
     * 获取文件包装类
     *
     * @param file 需要包装的文件对象
     * @return IFileWrapper 文件包装类实例
     */
    IFileWrapper getFileWrapper(Object file);
}

