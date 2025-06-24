package com.whim.file.config;

import com.whim.core.utils.SpringUtils;
import com.whim.file.FileStorageService;
import com.whim.file.adapter.ByteArrayAdapter;
import com.whim.file.adapter.IFileAdapter;
import com.whim.file.adapter.InputStreamAdapter;
import com.whim.file.adapter.LocalFileAdapter;
import com.whim.file.adapter.MultipartFileAdapter;
import com.whim.file.adapter.URIAdapter;
import com.whim.file.config.properties.FileStorageProperties;
import com.whim.file.storage.IFileStorage;
import com.whim.file.storage.impl.AliyunOssFileStorageImpl;
import com.whim.file.storage.impl.LocalFileStorageImpl;
import com.whim.file.storage.impl.MinioFileStorageImpl;
import org.apache.tika.Tika;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jince
 * date: 2025/6/24 12:44
 * description: 文件存储配置
 */
@AutoConfiguration
@EnableConfigurationProperties(FileStorageProperties.class)
public class FileStorageConfig {
    @Bean
    public FileStorageService fileStorageService() {
        // 创建适配器列表
        List<IFileAdapter> allFileAdapter = Arrays.asList(
                new ByteArrayAdapter(),
                new InputStreamAdapter(),
                new LocalFileAdapter(),
                new MultipartFileAdapter(),
                new URIAdapter()
        );

        // 创建IFileStorage实例列表
        Map<String, IFileStorage> storageMap = new HashMap<>();
        storageMap.put("aliyunOss", new AliyunOssFileStorageImpl());
        storageMap.put("local", new LocalFileStorageImpl());
        storageMap.put("minio", new MinioFileStorageImpl());

        return new FileStorageService(SpringUtils.getBean(FileStorageProperties.class), allFileAdapter, storageMap);
    }

    @Bean
    public Tika tika() {
        return new Tika();
    }
}
