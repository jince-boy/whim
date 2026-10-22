package com.whim.file.model;

import com.whim.core.exception.FileStorageException;
import com.whim.core.utils.IdUtils;
import com.whim.file.config.properties.FileStorageProperties;
import com.whim.file.support.LimitedInputStream;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 单次上传输入，统一管理文件流与远程连接的所有权。
 */
@Getter
public final class FileSource implements AutoCloseable {
    private final LimitedInputStream inputStream;
    private final long size;
    private final String fileName;
    private final String contentType;
    private final HttpURLConnection connection;

    /** 包装已打开的输入，未知大小使用 -1。 */
    private FileSource(InputStream inputStream, long size, String originalName, String contentType,
                       HttpURLConnection connection, FileStorageProperties properties, long deadline) {
        this.inputStream = new LimitedInputStream(inputStream, properties.getMaxFileSize().toBytes(), deadline);
        this.size = size;
        String extension = originalName == null ? "" : FilenameUtils.getExtension(originalName);
        this.fileName = IdUtils.uuid32() + (extension.matches("[a-zA-Z0-9]{1,16}") ? "." + extension : ".bin");
        this.contentType = contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
        this.connection = connection;
    }

    /** 适配常见文件输入；传入 InputStream 后，由本次上传负责关闭。 */
    public static FileSource open(Object file, FileStorageProperties properties) {
        InputStream input = null;
        HttpURLConnection remote = null;
        long deadline = System.nanoTime() + properties.getRequestTimeout().toNanos();
        try {
            long size;
            String originalName = null;
            String contentType = null;
            if (file instanceof MultipartFile multipart) {
                size = multipart.getSize();
                originalName = multipart.getOriginalFilename();
                contentType = multipart.getContentType();
                input = multipart.getInputStream();
            } else if (file instanceof byte[] bytes) {
                size = bytes.length;
                input = new ByteArrayInputStream(bytes);
            } else if (file instanceof InputStream stream) {
                size = -1;
                input = stream;
            } else if (file instanceof File || file instanceof Path) {
                Path path = file instanceof File localFile ? localFile.toPath() : (Path) file;
                size = Files.size(path);
                originalName = path.getFileName().toString();
                contentType = Files.probeContentType(path);
                input = Files.newInputStream(path);
            } else if (file instanceof URI uri) {
                String scheme = uri.getScheme();
                if (!("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))
                        || uri.getHost() == null || uri.getRawUserInfo() != null || uri.getRawFragment() != null
                        || properties.getRemoteAllowedHosts().stream()
                        .noneMatch(host -> host.equalsIgnoreCase(uri.getHost()))
                        || (uri.getPort() != -1 && uri.getPort() != ("https".equalsIgnoreCase(scheme) ? 443 : 80))) {
                    throw new FileStorageException("远程文件地址不在允许的 HTTP 或 HTTPS 主机范围内");
                }
                remote = (HttpURLConnection) uri.toURL().openConnection();
                remote.setInstanceFollowRedirects(false);
                remote.setConnectTimeout(Math.toIntExact(properties.getConnectTimeout().toMillis()));
                remote.setReadTimeout(Math.toIntExact(properties.getReadTimeout().toMillis()));
                remote.setRequestProperty("Accept-Encoding", "identity");
                int status = remote.getResponseCode();
                if (status != HttpURLConnection.HTTP_OK) {
                    throw new FileStorageException("远程文件请求失败，HTTP 状态码：" + status);
                }
                size = remote.getContentLengthLong();
                originalName = uri.getPath();
                contentType = remote.getContentType();
                input = remote.getInputStream();
            } else {
                throw new FileStorageException("不支持的文件输入类型");
            }
            if (size > properties.getMaxFileSize().toBytes()) {
                throw new FileStorageException("文件大小超过允许上限");
            }
            return new FileSource(input, size, originalName, contentType, remote, properties, deadline);
        } catch (Exception exception) {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException closeException) {
                    exception.addSuppressed(closeException);
                }
            }
            if (remote != null) {
                remote.disconnect();
            }
            if (exception instanceof FileStorageException storageException) {
                throw storageException;
            }
            throw new FileStorageException("打开上传文件失败", exception);
        }
    }

    /** 关闭输入流，并保证远程连接在异常情况下也能释放。 */
    @Override
    public void close() throws IOException {
        try {
            inputStream.close();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
