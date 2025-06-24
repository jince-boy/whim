package com.whim.file.adapter;

import com.whim.file.wrapper.IFileWrapper;
import com.whim.file.wrapper.LocalFileWrapper;

import java.io.File;

/**
 * @author jince
 * date: 2025/3/12 19:56
 * description: File文件适配器
 * 该类实现了IFileAdapter接口，用于适配File对象，将其转换为统一的文件处理接口
 */
public class LocalFileAdapter implements IFileAdapter {

    /**
     * 检查给定的文件对象是否是File实例
     *
     * @param file 待检查的文件对象
     * @return 如果是File实例返回true，否则返回false
     */
    @Override
    public boolean isSupport(Object file) {
        return file instanceof File;
    }

    /**
     * 将给定的文件对象包装为IFileWrapper接口的实现
     * 此方法假设传入的对象已经是File类型，因此直接进行类型转换并包装
     *
     * @param file File对象，预期为File类型
     * @return IFileWrapper接口的实现，用于统一处理文件对象
     */
    @Override
    public IFileWrapper getFileWrapper(Object file) {
        File fileObj = (File) file;
        return new LocalFileWrapper(fileObj);
    }
}

