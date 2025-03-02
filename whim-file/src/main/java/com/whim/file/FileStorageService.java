package com.whim.file;

import com.whim.file.adapter.IFileAdapter;
import com.whim.file.config.FileStorageProperties;
import com.whim.file.storage.IFileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author jince
 * date: 2025/2/18 13:47
 * description: 文件存储服务
 */
@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final FileStorageProperties fileStorageProperties;
    private final List<IFileAdapter> allFileAdapter;
    private final Map<String, IFileStorage> allFileStorage;

    public void upload(Object file) {
        this.upload(file, null);
    }

    public void upload(Object file, Consumer<FileOptions.Builder> configurator) {
        FileOptions.Builder builder = new FileOptions.Builder(fileStorageProperties, allFileAdapter, allFileStorage);
        builder.fileWrapper(file);
        if (Objects.nonNull(configurator)) {
            configurator.accept(builder);
        }
        FileOptions fileOptions = builder.build();
        allFileStorage.get(fileOptions.getPlatform()).upload(fileOptions);
    }
}
