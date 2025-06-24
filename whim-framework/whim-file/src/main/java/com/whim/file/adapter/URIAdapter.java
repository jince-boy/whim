package com.whim.file.adapter;

import com.whim.file.wrapper.IFileWrapper;
import com.whim.file.wrapper.URIWrapper;

import java.net.URI;

/**
 * @author jince
 * date: 2025/3/12 19:58
 * description: URL适配器，用于处理URI类型的文件
 */
public class URIAdapter implements IFileAdapter {

    /**
     * 判断是否支持给定的文件对象
     *
     * @param file 待判断的文件对象
     * @return 如果文件对象是URI类型，则返回true，否则返回false
     */
    @Override
    public boolean isSupport(Object file) {
        return file instanceof URI;
    }

    /**
     * 获取文件包装器
     *
     * @param file 需要包装的文件对象
     * @return 返回一个实现了IFileWrapper接口的URIWrapper实例
     */
    @Override
    public IFileWrapper getFileWrapper(Object file) {
        URI uri = (URI) file;
        return new URIWrapper(uri);
    }
}

