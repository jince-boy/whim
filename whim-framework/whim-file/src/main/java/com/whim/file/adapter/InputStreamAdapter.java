package com.whim.file.adapter;

import com.whim.file.wrapper.IFileWrapper;
import com.whim.file.wrapper.InputStreamWrapper;

import java.io.InputStream;

/**
 * @author jince
 * date: 2025/3/11 13:33
 * description: InputStream文件适配器
 * 该类实现了IFileAdapter接口，用于适配InputStream类型的文件对象
 * 它提供了判断是否支持传入的文件对象类型的方法，以及将支持的文件对象包装成IFileWrapper接口实现类的方法
 */
public class InputStreamAdapter implements IFileAdapter {

    /**
     * 判断是否支持传入的文件对象类型
     *
     * @param file 待判断的文件对象
     * @return 如果文件对象是InputStream类型，则返回true，否则返回false
     */
    @Override
    public boolean isSupport(Object file) {
        return file instanceof InputStream;
    }

    /**
     * 将支持的文件对象包装成IFileWrapper接口实现类
     *
     * @param file InputStream类型的文件对象
     * @return 返回一个以传入的InputStream为参数的InputStreamWrapper实例
     * 注意：该方法假设传入的file对象已经通过isSupport方法验证，确保其为InputStream类型
     */
    @Override
    public IFileWrapper getFileWrapper(Object file) {
        InputStream inputStream = (InputStream) file;
        return new InputStreamWrapper(inputStream);
    }
}

