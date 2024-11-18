package com.whim.file.storage;

import com.whim.file.FileInfo;
import com.whim.file.wrapper.IFileWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Jince
 * date: 2024/11/15 22:11
 * description:
 */
@Slf4j
@Component("local")
public class LocalStorage implements IFileStorage {
    @Override
    public Boolean upload(IFileWrapper fileWrapper, FileInfo fileInfo) {
//        Path basePath= FileUtils.
        return null;
    }
}
