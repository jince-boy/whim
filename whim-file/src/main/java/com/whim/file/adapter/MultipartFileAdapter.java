package com.whim.file.adapter;

import com.whim.file.wrapper.IFileWrapper;
import com.whim.file.wrapper.MultipartFileWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Jince
 * date: 2024/11/15 22:27
 * description: MultipartFile 文件适配器
 */
@Component
public class MultipartFileAdapter implements IFileAdapter {
    /**
     * 是否支持该文件类型
     *
     * @param file 文件
     * @return true 支持，false 不支持
     */
    @Override
    public boolean isSupport(Object file) {
        return file instanceof MultipartFile;
    }

    /**
     * 获取文件包装器
     *
     * @param file 文件
     * @return 文件包装器
     */
    @Override
    public IFileWrapper getFileWrapper(Object file) {
        MultipartFile multipartFile = (MultipartFile) file;
        return new MultipartFileWrapper(multipartFile);
    }
}
