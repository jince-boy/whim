package com.whim.file;

import com.whim.common.exception.FileStorageException;
import com.whim.file.adapter.IFileAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author Jince
 * date: 2025/2/10 21:57
 * description:
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final List<IFileAdapter> allFileAdapter;

    public FileStorageService wrap(Object file) {
        if (Objects.isNull(file)) {
            throw new FileStorageException("上传的文件不能为空");
        }
        for (IFileAdapter iFileAdapter : allFileAdapter) {
            if (iFileAdapter.isSupport(file)) {
                iFileAdapter.getFileWrapper(file);
                return this;
            }
        }
        throw new FileStorageException("没有找到支持的文件适配器");
    }


}
