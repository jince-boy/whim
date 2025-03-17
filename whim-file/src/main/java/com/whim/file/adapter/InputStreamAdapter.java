package com.whim.file.adapter;

import com.whim.file.adapter.wrapper.IFileWrapper;
import com.whim.file.adapter.wrapper.InputStreamWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * @author jince
 * date: 2025/3/11 13:33
 * description: InputStream文件适配器
 */
@Component
@RequiredArgsConstructor
public class InputStreamAdapter implements IFileAdapter {

    @Override
    public boolean isSupport(Object file) {
        return file instanceof InputStream;
    }

    @Override
    public IFileWrapper getFileWrapper(Object file) {
        InputStream inputStream = (InputStream) file;
        return new InputStreamWrapper(inputStream);
    }
}
