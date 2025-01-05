package com.whim.file;

import com.whim.common.exception.FileStorageException;
import com.whim.file.adapter.IFileAdapter;
import com.whim.file.handler.FileHandler;
import com.whim.file.storage.IFileStorage;
import com.whim.file.wrapper.IFileWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Jince
 * date: 2024/11/14 23:08
 * description: 文件存储服务
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final FileStorageProperties fileStorageProperties;
    private final Map<String, IFileStorage> allFileStorage;
    private final List<IFileAdapter> allFileAdapter;
    private IFileWrapper fileWrapper;
    private FileHandler fileHandler;

    /**
     * 包装文件
     *
     * @param file 文件对象
     * @return FileStorageService
     */
    public FileStorageService wrap(Object file) {
        if (Objects.isNull(file)) {
            throw new FileStorageException("文件不能为空");
        }
        for (IFileAdapter iFileAdapter : allFileAdapter) {
            if (iFileAdapter.isSupport(file)) {
                this.fileWrapper = iFileAdapter.getFileWrapper(file);
                return this;
            }
        }
        throw new FileStorageException("没有找到支持的文件适配器");
    }

    /**
     * 文件处理器方法
     *
     * @param fileConfig 配置文件处理逻辑的消费者。通过此消费者，可以调用 {@link FileHandler} 提供的 API 来设置文件相关属性.
     * @return FileStorageService
     */
    public FileStorageService createFileHandler(Consumer<FileHandler> fileConfig) {
        FileHandler fileHandler = new FileHandler();
        fileConfig.accept(fileHandler);
        return this;
    }

    /**
     * 图片处理器方法
     *
     * @param consumer 配置图片处理逻辑的消费者。通过此消费者，可以调用 {@link Thumbnails.Builder} 提供的 API 进行图片处理操作。
     * @return FileStorageService
     */
    public FileStorageService createImageHandler(Consumer<Thumbnails.Builder<? extends InputStream>> consumer) {
        if (this.fileWrapper.getFileContentType().startsWith("image/")) {
            try {
                InputStream inputStream = this.fileWrapper.getInputStream();
                Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(inputStream);
                consumer.accept(builder);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                builder.toOutputStream(outputStream);
                InputStream processedInputStream = new ByteArrayInputStream(outputStream.toByteArray());
                this.fileWrapper.setFileSize((long) outputStream.size());
                this.fileWrapper.setInputStream(processedInputStream);
            } catch (IOException e) {
                throw new FileStorageException("图片处理失败", e);
            }
        } else {
            throw new FileStorageException("不是图片类型的对象，无法处理进行图像处理");
        }
        return this;
    }

    /**
     * 上传文件
     *
     * @return FileInfo 上传文件信息
     */
    public FileInfo upload() {
//        if ()
            return new FileInfo();
    }

}
