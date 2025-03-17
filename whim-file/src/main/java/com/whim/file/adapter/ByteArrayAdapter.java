package com.whim.file.adapter;

import com.whim.file.adapter.wrapper.ByteArrayWrapper;
import com.whim.file.adapter.wrapper.IFileWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author jince
 * date: 2025/3/12 19:46
 * description: byte数组适配器
 */
@Component
@RequiredArgsConstructor
public class ByteArrayAdapter implements IFileAdapter {

    @Override
    public boolean isSupport(Object file) {
        return file instanceof byte[];
    }

    @Override
    public IFileWrapper getFileWrapper(Object file) {
        byte[] byteArray = (byte[]) file;
        return new ByteArrayWrapper(byteArray);
    }
}
