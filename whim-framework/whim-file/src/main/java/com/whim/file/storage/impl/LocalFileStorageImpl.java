package com.whim.file.storage.impl;

import com.whim.core.exception.FileStorageException;
import com.whim.file.config.properties.FileStorageProperties.LocalStorageProperties;
import com.whim.file.handler.DownloadHandler;
import com.whim.file.model.FileSource;
import com.whim.file.model.MetaData;
import com.whim.file.storage.BaseFileStorage;
import com.whim.file.support.StoragePath;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 本地文件存储，限制根目录及符号链接，使用同目录临时文件原子替换。
 */
public final class LocalFileStorageImpl extends BaseFileStorage {
    private final Path root;

    /** 创建受信任的配置根目录并固定其真实位置。 */
    public LocalFileStorageImpl(LocalStorageProperties properties) {
        super("local", properties.getName());
        try {
            Path configuredRoot = Path.of(properties.getBasePath()).toAbsolutePath().normalize();
            Files.createDirectories(configuredRoot);
            root = configuredRoot.toRealPath();
        } catch (IOException exception) {
            throw new FileStorageException("初始化本地文件存储目录失败", exception);
        }
    }

    /** 先完成临时文件写入，再原子替换目标；失败时保留原文件。 */
    @Override
    public MetaData upload(FileSource source, String key, String contentType) {
        Path temporary = null;
        try {
            Path target = resolveFile(key, true);
            temporary = Files.createTempFile(target.getParent(), ".whim-upload-", ".tmp");
            long size;
            try (OutputStream output = Files.newOutputStream(temporary, StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING, LinkOption.NOFOLLOW_LINKS)) {
                size = source.getInputStream().transferTo(output);
            }
            if (source.getSize() >= 0 && size != source.getSize()) {
                throw new FileStorageException("上传文件实际大小与声明大小不一致");
            }
            resolveFile(key, false);
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            return createMetaData(key, size, contentType);
        } catch (Exception exception) {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException cleanupException) {
                    exception.addSuppressed(cleanupException);
                }
            }
            throw new FileStorageException("本地文件上传失败", exception);
        }
    }

    /** 打开普通文件，拒绝目录及符号链接。 */
    @Override
    public DownloadHandler download(String key) {
        try {
            Path path = resolveFile(key, false);
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                throw new FileStorageException("文件不存在或不是普通文件");
            }
            InputStream input = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS);
            return DownloadHandler.of(input);
        } catch (IOException exception) {
            throw new FileStorageException("本地文件下载失败", exception);
        }
    }

    /** 查询本地文件大小，内容类型按文件名及操作系统探测。 */
    @Override
    public MetaData getFileMetaData(String key) {
        try {
            Path path = resolveFile(key, false);
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                throw new FileStorageException("文件不存在或不是普通文件");
            }
            return createMetaData(key, Files.size(path), Files.probeContentType(path));
        } catch (IOException exception) {
            throw new FileStorageException("获取本地文件信息失败", exception);
        }
    }

    /** 判断普通文件是否存在。 */
    @Override
    public boolean exists(String key) {
        try {
            Path path = resolveFile(key, false);
            return Files.readAttributes(path, java.nio.file.attribute.BasicFileAttributes.class,
                    LinkOption.NOFOLLOW_LINKS).isRegularFile();
        } catch (java.nio.file.NoSuchFileException exception) {
            return false;
        } catch (IOException exception) {
            throw new FileStorageException("检查本地文件失败", exception);
        }
    }

    /** 幂等删除文件，不允许删除目录或符号链接。 */
    @Override
    public void deleteFile(String key) {
        try {
            Path path = resolveFile(key, false);
            if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                throw new FileStorageException("不允许删除文件目录");
            }
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new FileStorageException("删除本地文件失败", exception);
        }
    }

    /** 逐级校验路径，上传时仅创建真实目录；根目录必须由应用独占写入。 */
    private Path resolveFile(String key, boolean createDirectories) throws IOException {
        Path target = root.resolve(StoragePath.validateKey(key)).normalize();
        if (!target.startsWith(root) || target.equals(root)) {
            throw new FileStorageException("文件路径超出存储根目录");
        }
        if (Files.isSymbolicLink(root) || !root.toRealPath().equals(root)) {
            throw new FileStorageException("存储根目录已发生变化");
        }
        Path current = root;
        for (Path segment : root.relativize(target.getParent())) {
            current = current.resolve(segment);
            if (createDirectories) {
                try {
                    Files.createDirectory(current);
                } catch (FileAlreadyExistsException ignored) {
                    // 已存在的目录仍须通过下方校验。
                }
            }
            if (Files.isSymbolicLink(current)
                    || (Files.exists(current, LinkOption.NOFOLLOW_LINKS)
                    && (!Files.isDirectory(current, LinkOption.NOFOLLOW_LINKS)
                    || !current.toRealPath().startsWith(root)))) {
                throw new FileStorageException("文件路径包含符号链接或非目录节点");
            }
        }
        if (Files.isSymbolicLink(target)) {
            throw new FileStorageException("不允许操作符号链接文件");
        }
        return target;
    }
}
