package com.whim.file;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author jince
 * date: 2025/2/17 21:51
 * description: MultipartFile适配器
 */
@Component
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
