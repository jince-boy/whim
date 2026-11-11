package com.whim.file.support;

import com.whim.core.exception.FileStorageException;
import com.whim.file.handler.DownloadHandler;
import com.whim.file.model.MetaData;
import com.whim.file.storage.IFileStorage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.ParseContext;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;

/**
 * @author Jince
 * @date 2026/11/11
 * @description 文件主动内容校验及安全响应约束，原文件始终作为附件，预览必须重新编码。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUploadSecurity {
    public static final int DETECTION_BYTES = 64 * 1024;
    public static final String DOWNLOAD_CONTENT_TYPE = MediaType.APPLICATION_OCTET_STREAM_VALUE;
    public static final String CONTENT_SECURITY_POLICY = "sandbox; default-src 'none'; base-uri 'none'; form-action 'none'";
    private static final MimeTypes MIME_TYPES = MimeTypes.getDefaultMimeTypes();
    private static final Set<String> ACTIVE_EXTENSIONS = Set.of(
            "html", "htm", "shtml", "shtm", "xhtml", "xht", "svg", "svgz", "xml", "xsl", "xslt",
            "js", "mjs", "cjs", "css", "hta", "mht", "mhtml", "swf");
    private static final Set<String> ACTIVE_CONTENT_TYPES = Set.of(
            "text/html", "text/xml", "application/xml", "text/css", "text/javascript",
            "application/javascript", "application/x-javascript", "text/ecmascript", "application/ecmascript",
            "text/jscript", "text/livescript", "application/hta", "application/x-shockwave-flash",
            "message/rfc822", "multipart/related");

    /**
     * 在读取或创建上传任务前拒绝主动内容后缀及不安全的客户端类型声明。
     *
     * @param fileName 原文件名称或对象标识，无名称的流可以为空
     * @param contentType 客户端声明的类型，可以为空
     */
    public static void validateMetadata(String fileName, String contentType) {
        if (fileName != null) {
            String name = fileName.replace('\\', '/');
            name = name.substring(name.lastIndexOf('/') + 1);
            // 连续后缀和大小写不能将页面文件伪装成普通附件，例如 page.HTML.txt。
            String[] segments = name.toLowerCase(Locale.ROOT).split("\\.");
            for (int index = 1; index < segments.length; index++) {
                if (ACTIVE_EXTENSIONS.contains(segments[index].strip())) {
                    throw new IllegalArgumentException("不允许上传HTML、SVG、XML、脚本或其他浏览器主动内容文件");
                }
            }
        }
        if (contentType == null || contentType.isBlank()) {
            return;
        }
        if (contentType.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("文件内容类型包含不允许的控制字符");
        }
        MediaType parsed;
        try {
            parsed = MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("文件内容类型格式无效", exception);
        }
        String type = (parsed.getType() + "/" + parsed.getSubtype()).toLowerCase(Locale.ROOT);
        if (parsed.isWildcardType() || parsed.isWildcardSubtype()
                || ACTIVE_CONTENT_TYPES.contains(type) || type.endsWith("+xml")) {
            throw new IllegalArgumentException("不允许上传HTML、SVG、XML、脚本或其他浏览器主动内容文件");
        }
    }

    /**
     * 只依据文件头识别类型并拒绝主动内容，不使用客户端文件名或MIME提示。
     *
     * @param prefix 不超过64KiB的文件头
     * @return 检测出的内容类型，不能识别时为二进制类型
     */
    public static String getContentType(byte[] prefix) {
        if (prefix.length > DETECTION_BYTES) {
            throw new IllegalArgumentException("文件类型检测数据超过允许上限");
        }
        try (TikaInputStream input = TikaInputStream.get(prefix)) {
            ParseContext context = new ParseContext();
            String contentType = MIME_TYPES.detect(input, Metadata.newInstance(context), context).toString();
            validateMetadata(null, contentType);
            // 文件头检测只是纵深校验；不据此允许原文件在浏览器内联执行。
            return contentType;
        } catch (IOException exception) {
            throw new FileStorageException("检测文件实际类型失败", exception);
        }
    }

    /**
     * 有界读取合并文件的文件头进行校验，流由调用方关闭。
     *
     * @param source 已打开的文件输入流，读取后不回退
     * @return 实际检测出的内容类型
     */
    public static String getContentType(InputStream source) {
        try {
            LimitedInputStream input = new LimitedInputStream(source, DETECTION_BYTES,
                    System.nanoTime() + Duration.ofSeconds(30).toNanos());
            return getContentType(input.readNBytes(DETECTION_BYTES));
        } catch (IOException exception) {
            throw new FileStorageException("读取文件安全校验数据失败", exception);
        }
    }

    /**
     * 校验存储中的合并文件，拒绝时删除对象，防止分片路径跳过内容检测。
     *
     * @param storage 文件实际存储实例
     * @param key 文件存储标识
     * @return 大小及实际检测类型均来自存储内容的元数据
     */
    public static MetaData getValidatedFileMetaData(IFileStorage storage, String key) {
        try {
            validateMetadata(key, null);
            MetaData metadata = storage.getFileMetaData(key);
            try (DownloadHandler download = storage.download(key)) {
                metadata.setContentType(getContentType(download.getInputStream()));
            }
            return metadata;
        } catch (IllegalArgumentException exception) {
            // 仅删除明确违反安全策略的对象；存储故障保留现场供重试。
            try {
                storage.deleteFile(key);
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }
    }
}
