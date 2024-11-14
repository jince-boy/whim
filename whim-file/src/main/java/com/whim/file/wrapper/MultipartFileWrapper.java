package com.whim.file.wrapper;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author Jince
 * date: 2024/11/14 23:13
 * description: MultipartFile 包装器
 */
public class MultipartFileWrapper {
    private final MultipartFile file;

    public MultipartFileWrapper(MultipartFile file) {
        this.file = file;
    }

}
