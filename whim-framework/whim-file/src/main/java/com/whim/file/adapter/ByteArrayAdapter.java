package com.whim.file.adapter;

import com.whim.file.wrapper.ByteArrayWrapper;
import com.whim.file.wrapper.IFileWrapper;

/**
 * @author jince
 * @date 2025/3/12 19:46
 * @description 适配字节数组的适配器类，实现IFileAdapter接口
 * 该类的目的是将字节数组转换为文件包装器，以便统一处理不同类型的文件
 */
public class ByteArrayAdapter implements IFileAdapter {

    /**
     * 检查当前适配器是否支持处理传入的文件对象
     * 仅当文件对象为字节数组时，该适配器才支持
     *
     * @param file 待检查的文件对象
     * @return 如果文件是字节数组，则返回true，否则返回false
     */
    @Override
    public boolean isSupport(Object file) {
        return file instanceof byte[];
    }

    /**
     * 获取文件包装器对象，用于处理传入的文件对象
     * 该方法假设传入的文件对象已通过isSupport方法验证，确保其为字节数组
     * 它将字节数组转换为ByteArrayWrapper实例，以提供额外的功能或抽象
     *
     * @param file 已验证的字节数组
     * @return 包装了字节数组的ByteArrayWrapper实例
     */
    @Override
    public IFileWrapper getFileWrapper(Object file) {
        byte[] byteArray = (byte[]) file;
        return new ByteArrayWrapper(byteArray);
    }
}

