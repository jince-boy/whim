package com.whim.excel.utils;

import com.whim.core.exception.ServiceException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * @author Jince
 * @date 2026/10/25
 * @description Excel 下载响应头，保留中文文件名并拒绝控制字符和路径分隔符。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExcelResponseUtils {

    /** 写入下载响应，提交前发生异常时清理附件响应，交给全局异常处理器返回错误。 */
    public static void write(HttpServletResponse response, String filename, boolean zip, Consumer<OutputStream> writer) {
        prepare(response, filename, zip);
        try {
            writer.accept(response.getOutputStream());
        } catch (IOException | RuntimeException exception) {
            if (!response.isCommitted()) {
                response.resetBuffer();
                response.setHeader(HttpHeaders.CONTENT_DISPOSITION, null);
                response.setContentType(null);
            }
            if (exception instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new ServiceException("获取 Excel 下载输出流失败", exception);
        }
    }

    /** 设置附件名称和 MIME 类型，输出流继续由 Servlet 容器管理。 */
    public static void prepare(HttpServletResponse response, String filename, boolean zip) {
        Assert.state(!response.isCommitted(), "HTTP 响应已经提交，无法导出 Excel");
        Assert.hasText(filename, "导出文件名不能为空");
        Assert.isTrue(
            filename.codePoints().noneMatch(Character::isISOControl) &&
                !filename.contains("/") &&
                !filename.contains("\\"),
            "导出文件名包含非法字符"
        );
        String extension = zip ? ".zip" : ".xlsx";
        String completeName = filename.endsWith(extension) ? filename : filename + extension;
        response.setContentType(
            zip ? "application/zip" : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        response.setHeader(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment().filename(completeName, StandardCharsets.UTF_8).build().toString()
        );
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
    }
}
