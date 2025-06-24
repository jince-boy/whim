package com.whim.file.adapter;

import com.whim.file.wrapper.IFileWrapper;
import com.whim.file.wrapper.MultipartFileWrapper;
import org.springframework.web.multipart.MultipartFile;

/**
 * MultipartFile适配器
 * 该适配器用于处理MultipartFile类型的文件，将其转换为统一的文件处理接口
 * 主要作用是适配不同的文件处理方式，以便在系统中统一管理文件操作
 */
public class MultipartFileAdapter implements IFileAdapter {

    /**
     * 检查文件是否支持MultipartFile类型
     *
     * @param file 待检查的文件对象
     * @return 如果文件是MultipartFile类型，则返回true，否则返回false
     */
    @Override
    public boolean isSupport(Object file) {
        return file instanceof MultipartFile;
    }

    /**
     * 获取文件包装器
     * 将传入的MultipartFile对象包装成MultipartFileWrapper，以提供统一的文件操作接口
     *
     * @param file MultipartFile类型的文件对象
     * @return 返回MultipartFileWrapper对象，提供对文件的操作方法
     */
    @Override
    public IFileWrapper getFileWrapper(Object file) {
        MultipartFile multipartFile = (MultipartFile) file;
        return new MultipartFileWrapper(multipartFile);
    }
}

