package com.whim.file.adapter;

import com.whim.file.adapter.wrapper.FileWrapper;
import com.whim.file.adapter.wrapper.IFileWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author jince
 * date: 2025/3/12 19:56
 * description: File文件适配器
 */
@Component
@RequiredArgsConstructor
public class FileAdapter implements IFileAdapter {

    @Override
    public boolean isSupport(Object file) {
        return file instanceof File;
    }

    @Override
    public IFileWrapper getFileWrapper(Object file) {
        File fileObj = (File) file;
        return new FileWrapper(fileObj);
    }
}
