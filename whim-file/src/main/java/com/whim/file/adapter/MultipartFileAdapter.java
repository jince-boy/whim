package com.whim.file.adapter;

import com.whim.file.wrapper.IFileWrapper;
import com.whim.file.wrapper.MultipartFileWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author jince
 * date: 2025/2/17 21:51
 * description: MultipartFile适配器
 */
@Component
@RequiredArgsConstructor
public class MultipartFileAdapter implements IFileAdapter {

    @Override
    public boolean isSupport(Object file) {
        return file instanceof MultipartFile;
    }

    @Override
    public IFileWrapper getFileWrapper(Object file) {
        MultipartFile multipartFile = (MultipartFile) file;
        return new MultipartFileWrapper(multipartFile);
    }
}
