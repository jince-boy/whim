package com.whim.core.crypto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Base64;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Jince
 * @date 2026/11/22
 * @description 安全创建并读取多个实例共用的持久化密钥文件。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PersistentKeyFile {

    /**
     * 加载已有密钥，首次使用时原子保存新生成的密钥。
     *
     * @param path 密钥文件位置
     * @param generator 首次创建文件时使用的密钥生成器
     * @return 单行 Base64 密钥
     * @throws IOException 文件无法访问
     */
    public static String loadOrCreate(Path path, Supplier<byte[]> generator) throws IOException {
        if (path.toString().isBlank()) {
            throw new IllegalArgumentException("密钥文件路径不能为空");
        }
        Path keyPath = path.toAbsolutePath().normalize();
        boolean posix = keyPath.getFileSystem().supportedFileAttributeViews().contains("posix");
        FileAttribute<?>[] directoryAttributes = posix
                ? new FileAttribute<?>[]{PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"))}
                : new FileAttribute<?>[0];
        FileAttribute<?>[] fileAttributes = posix
                ? new FileAttribute<?>[]{PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"))}
                : new FileAttribute<?>[0];
        Files.createDirectories(keyPath.getParent(), directoryAttributes);

        // 共用目录的进程在初始化时互斥，避免同时启动生成不同密钥；锁文件不能删除后重建。
        Path lockPath = keyPath.resolveSibling(keyPath.getFileName() + ".lock");
        try (FileChannel lockChannel = FileChannel.open(lockPath,
                Set.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE), fileAttributes);
             FileLock initializationLock = lockChannel.lock()) {
            if (Files.notExists(keyPath)) {
                byte[] encoded = Base64.getEncoder().encode(generator.get());
                Path temporaryPath = Files.createTempFile(keyPath.getParent(), ".key-", ".tmp", fileAttributes);
                try {
                    // 先完整写入并刷盘，再原子发布，进程中断不会留下半份有效文件。
                    try (FileChannel output = FileChannel.open(temporaryPath, StandardOpenOption.WRITE)) {
                        ByteBuffer buffer = ByteBuffer.wrap(encoded);
                        while (buffer.hasRemaining()) {
                            output.write(buffer);
                        }
                        output.force(true);
                    }
                    Files.move(temporaryPath, keyPath, StandardCopyOption.ATOMIC_MOVE);
                } finally {
                    Files.deleteIfExists(temporaryPath);
                }
            }
            // 已存在的密钥损坏或不可读时直接启动失败，不能悄悄换钥影响在线请求。
            if (Files.size(keyPath) > 16384) {
                throw new IllegalArgumentException("密钥文件内容过长");
            }
            return Files.readString(keyPath, StandardCharsets.US_ASCII).strip();
        }
    }
}
