package com.whim.file.storage.impl;

import com.whim.core.exception.FileStorageException;
import com.whim.file.config.properties.FileStorageProperties.LocalStorageProperties;
import com.whim.file.handler.DownloadHandler;
import com.whim.file.model.FileSource;
import com.whim.file.model.MetaData;
import com.whim.file.exception.MissingUploadSessionException;
import com.whim.file.model.StoragePart;
import com.whim.file.storage.MultipartFileStorage;
import com.whim.file.storage.BaseFileStorage;
import com.whim.file.support.StoragePath;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 本地文件存储，限制根目录及符号链接，使用同目录临时文件原子替换。
 */
public final class LocalFileStorageImpl extends BaseFileStorage implements MultipartFileStorage {
    private static final String MULTIPART_DIRECTORY = ".multipart";
    private static final long PART_MAGIC = 0x5748494D50525431L;
    private static final int PART_HEADER_SIZE = Long.BYTES * 2 + 16 + 32;
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

    /** 本地上传通过应用接口传输分片。 */
    @Override
    public boolean supportsDirectUpload() {
        return false;
    }

    /** 使用任务 ID 创建独立目录，重复初始化同一任务不会生成孤儿目录。 */
    @Override
    public String initiateUpload(String key, String contentType, String taskId) {
        try {
            resolveFile(key, false);
            Path marker = resolveUploadFile(taskId, "session", true);
            try {
                Files.createFile(marker);
            } catch (FileAlreadyExistsException ignored) {
                // 重试使用同一会话目录。
            }
            return taskId;
        } catch (IOException exception) {
            throw new FileStorageException("创建本地分片任务失败", exception);
        }
    }

    /** 仅读取已提交分片的固定长度头部和文件大小，不扫描分片正文。 */
    @Override
    public List<StoragePart> getUploadedParts(String key, String uploadId) {
        try {
            resolveFile(key, false);
            Path marker = resolveUploadFile(uploadId, "session", false);
            if (!Files.isRegularFile(marker, LinkOption.NOFOLLOW_LINKS)) {
                throw new MissingUploadSessionException(null);
            }
            List<StoragePart> result = new ArrayList<>();
            try (var entries = Files.list(marker.getParent())) {
                List<Path> paths = entries.limit(20003).toList();
                if (paths.size() > 20002) {
                    throw new FileStorageException("本地分片目录文件数量异常");
                }
                for (Path entry : paths) {
                    String name = entry.getFileName().toString();
                    if (name.matches("[1-9][0-9]{0,3}\\.part") || name.equals("10000.part")) {
                        Path part = resolveUploadFile(uploadId, name, false);
                        int number = Integer.parseInt(name.substring(0, name.indexOf('.')));
                        try (DataInputStream input = new DataInputStream(
                                Files.newInputStream(part, LinkOption.NOFOLLOW_LINKS))) {
                            result.add(getPartMetaData(input, number, Files.size(part)));
                        }
                    }
                }
            }
            result.sort(Comparator.comparingInt(StoragePart::getPartNumber));
            return result;
        } catch (Exception exception) {
            if (exception instanceof MissingUploadSessionException missing) {
                throw missing;
            }
            throw new FileStorageException("查询本地分片失败", exception);
        }
    }

    /** 限长写入并验证摘要，将元数据与正文一并原子提交；失败时保留原分片。 */
    @Override
    public void uploadPart(String key, String uploadId, int partNumber, InputStream input,
                           long size, String contentMd5, String contentSha256) {
        Path temporary = null;
        try {
            resolveFile(key, false);
            if (partNumber < 1 || partNumber > 10000 || size < 0) {
                throw new FileStorageException("分片序号或大小无效");
            }
            byte[] expectedMd5 = Base64.getDecoder().decode(contentMd5);
            byte[] expectedSha256 = Base64.getDecoder().decode(contentSha256);
            if (expectedMd5.length != 16 || expectedSha256.length != 32) {
                throw new FileStorageException("分片摘要长度无效");
            }
            Path target = resolveUploadFile(uploadId, partNumber + ".part", false);
            if (!Files.isRegularFile(target.getParent().resolve("session"), LinkOption.NOFOLLOW_LINKS)) {
                throw new FileStorageException("本地分片会话不存在");
            }
            temporary = resolveUploadFile(uploadId, partNumber + ".writing", false);
            MessageDigest digest = MessageDigest.getInstance("MD5");
            MessageDigest strongDigest = MessageDigest.getInstance("SHA-256");
            long copied = 0;
            try (DataOutputStream output = new DataOutputStream(Files.newOutputStream(temporary, StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, LinkOption.NOFOLLOW_LINKS))) {
                // 元数据与正文在同一文件内原子发布，查询不会读到不同版本的大小和摘要。
                output.writeLong(PART_MAGIC);
                output.writeLong(size);
                output.write(expectedMd5);
                output.write(expectedSha256);
                byte[] buffer = new byte[65536];
                while (copied < size) {
                    int count = input.read(buffer, 0, (int) Math.min(buffer.length, size - copied));
                    if (count == -1) {
                        throw new FileStorageException("分片实际大小小于声明大小");
                    }
                    output.write(buffer, 0, count);
                    digest.update(buffer, 0, count);
                    strongDigest.update(buffer, 0, count);
                    copied += count;
                }
                if (input.read() != -1) {
                    throw new FileStorageException("分片实际大小超过声明大小");
                }
            }
            if (!MessageDigest.isEqual(digest.digest(), expectedMd5)
                    || !MessageDigest.isEqual(strongDigest.digest(), expectedSha256)) {
                throw new FileStorageException("分片内容校验失败");
            }
            resolveUploadFile(uploadId, partNumber + ".part", false);
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException cleanup) {
                    exception.addSuppressed(cleanup);
                }
            }
            throw new FileStorageException("写入本地分片失败", exception);
        }
    }

    /** 逐片复核正文并原子发布正式文件，成功后清理会话；清理失败可重试 abortUpload。 */
    @Override
    public void completeUpload(String key, String uploadId, List<StoragePart> parts, long size, String contentType) {
        Path temporary = null;
        try {
            Path target = resolveFile(key, true);
            if (!Files.isRegularFile(resolveUploadFile(uploadId, "session", false), LinkOption.NOFOLLOW_LINKS)) {
                throw new MissingUploadSessionException(null);
            }
            temporary = resolveUploadFile(uploadId, "merging.tmp", false);
            long total = 0;
            try (OutputStream output = Files.newOutputStream(temporary, StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, LinkOption.NOFOLLOW_LINKS)) {
                byte[] buffer = new byte[65536];
                for (StoragePart part : parts) {
                    MessageDigest digest = MessageDigest.getInstance("MD5");
                    MessageDigest strongDigest = MessageDigest.getInstance("SHA-256");
                    long copied = 0;
                    Path path = resolveUploadFile(uploadId, part.getPartNumber() + ".part", false);
                    try (DataInputStream input = new DataInputStream(Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS))) {
                        StoragePart stored = getPartMetaData(input, part.getPartNumber(), Files.size(path));
                        if (stored.getSize() != part.getSize() || !stored.getEtag().equals(part.getEtag())
                                || !stored.getContentSha256().equals(part.getContentSha256())) {
                            throw new FileStorageException("合并时分片元数据发生变化");
                        }
                        int count;
                        while ((count = input.read(buffer)) != -1) {
                            copied += count;
                            if (copied > part.getSize()) {
                                throw new FileStorageException("合并时分片大小发生变化");
                            }
                            digest.update(buffer, 0, count);
                            strongDigest.update(buffer, 0, count);
                            output.write(buffer, 0, count);
                        }
                    }
                    if (copied != part.getSize() || !HexFormat.of().formatHex(digest.digest()).equals(part.getEtag())
                            || !Base64.getEncoder().encodeToString(strongDigest.digest()).equals(part.getContentSha256())) {
                        throw new FileStorageException("合并时分片内容发生变化");
                    }
                    total += copied;
                }
            }
            if (total != size) {
                throw new FileStorageException("合并文件总大小不一致");
            }
            resolveFile(key, false);
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception exception) {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException cleanup) {
                    exception.addSuppressed(cleanup);
                }
            }
            throw new FileStorageException("合并本地分片失败", exception);
        }
        try {
            abortUpload(key, uploadId);
        } catch (FileStorageException exception) {
            throw new FileStorageException("本地文件已合并，但分片清理失败，请重试终止会话", exception);
        }
    }

    /** 仅清理当前会话目录的普通文件，不递归跟随链接或删除正式对象。 */
    @Override
    public void abortUpload(String key, String uploadId) {
        try {
            resolveFile(key, false);
            Path directory = resolveUploadFile(uploadId, "session", false).getParent();
            if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
                return;
            }
            // 先撤销会话标记，部分清理失败时也不能再继续上传或合并。
            Files.deleteIfExists(resolveUploadFile(uploadId, "session", false));
            try (var entries = Files.list(directory)) {
                List<Path> paths = entries.limit(20003).toList();
                if (paths.size() > 20002) {
                    throw new FileStorageException("本地分片目录文件数量异常");
                }
                for (Path path : paths) {
                    Path checked = resolveUploadFile(uploadId, path.getFileName().toString(), false);
                    if (!Files.isRegularFile(checked, LinkOption.NOFOLLOW_LINKS)) {
                        throw new FileStorageException("分片目录包含非普通文件");
                    }
                    Files.deleteIfExists(checked);
                }
            }
            Files.deleteIfExists(directory);
        } catch (IOException exception) {
            throw new FileStorageException("清理本地分片失败", exception);
        }
    }

    /** 读取版本化头部并核对正文长度，调用后输入流恰好位于正文起点。 */
    private StoragePart getPartMetaData(DataInputStream input, int partNumber, long storedSize) throws IOException {
        if (storedSize < PART_HEADER_SIZE || input.readLong() != PART_MAGIC) {
            throw new FileStorageException("本地分片格式无效或版本不兼容，请终止会话后重新上传");
        }
        long size = input.readLong();
        if (size < 0 || size != storedSize - PART_HEADER_SIZE) {
            throw new FileStorageException("本地分片大小与元数据不一致");
        }
        byte[] md5 = new byte[16];
        byte[] sha256 = new byte[32];
        input.readFully(md5);
        input.readFully(sha256);
        return new StoragePart(partNumber, size, HexFormat.of().formatHex(md5),
                Base64.getEncoder().encodeToString(sha256));
    }

    /** 上传会话只能使用系统生成的 32 位标识，内部目录同样执行路径安全校验。 */
    private Path resolveUploadFile(String uploadId, String name, boolean createDirectories) throws IOException {
        if (uploadId == null || !uploadId.matches("[a-f0-9]{32}")) {
            throw new FileStorageException("本地上传会话标识无效");
        }
        return resolveStorageFile(MULTIPART_DIRECTORY + "/" + uploadId + "/" + name, createDirectories);
    }

    /** 普通文件不得占用内部分片目录，同时拒绝大小写变体及已有目录的短文件名别名。 */
    private Path resolveFile(String key, boolean createDirectories) throws IOException {
        StoragePath.validateKey(key);
        int separator = key.indexOf('/');
        String firstSegment = separator < 0 ? key : key.substring(0, separator);
        if (MULTIPART_DIRECTORY.equalsIgnoreCase(firstSegment)) {
            throw new FileStorageException("文件路径不能使用内部分片目录");
        }
        Path firstPath = root.resolve(firstSegment);
        if (Files.exists(firstPath, LinkOption.NOFOLLOW_LINKS)
                && firstPath.toRealPath().equals(root.resolve(MULTIPART_DIRECTORY))) {
            throw new FileStorageException("文件路径不能使用内部分片目录的别名");
        }
        return resolveStorageFile(key, createDirectories);
    }

    /** 逐级校验业务及内部路径，上传时仅创建真实目录；根目录必须由应用独占写入。 */
    private Path resolveStorageFile(String key, boolean createDirectories) throws IOException {
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
