package com.whim.file.storage.impl;

import com.whim.file.FileOptions;
import com.whim.file.model.FileInfo;
import com.whim.file.storage.IFileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author jince
 * date: 2025/3/18 21:22
 * description: 阿里云oss文件存储实现
 */
@Slf4j
@Component("aliyun-oss")
@RequiredArgsConstructor
public class AliyunOssFileStorageImpl implements IFileStorage {
    @Override
    public Boolean upload(FileOptions fileOptions) {
        return null;
    }

    @Override
    public FileInfo getFileInfo(FileOptions fileOptions) {
        return null;
    }

    @Override
    public Boolean deleteFile(FileOptions fileOptions) {
        return null;
    }

    @Override
    public Boolean exists(FileOptions fileOptions) {
        return null;
    }

    @Override
    public List<String> list(FileOptions fileOptions) {
        return List.of();
    }

    @Override
    public String getFilePreSignedUrl(FileOptions fileOptions) {
        return "";
    }

    @Override
    public String getFilePreSignedUrl(FileOptions fileOptions, Integer expire, TimeUnit timeUnit) {
        return "";
    }

    @Override
    public String uploadFilePreSignedUrl(FileOptions fileOptions, Integer expire, TimeUnit timeUnit) {
        return "";
    }
}
