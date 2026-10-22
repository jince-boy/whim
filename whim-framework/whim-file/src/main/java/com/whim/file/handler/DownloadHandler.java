package com.whim.file.handler;

import com.whim.core.exception.FileStorageException;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 单次下载流，支持显式关闭及消费后自动释放；不关闭调用方输出流。
 */
public final class DownloadHandler implements AutoCloseable {
    private static final int DEFAULT_ARRAY_LIMIT = 20 * 1024 * 1024;
    @Getter
    private final InputStream inputStream;
    private final AtomicBoolean closed = new AtomicBoolean();

    /** 包装底层响应流，关闭操作只执行一次。 */
    private DownloadHandler(InputStream source) {
        inputStream = new FilterInputStream(source) {
            /** 释放底层流，重复关闭无副作用。 */
            @Override
            public void close() throws IOException {
                if (closed.compareAndSet(false, true)) {
                    super.close();
                }
            }
        };
    }

    /** 创建拥有输入流所有权的下载处理器。 */
    public static DownloadHandler of(InputStream inputStream) {
        return new DownloadHandler(inputStream);
    }

    /** 消费输入流并自动释放资源。 */
    public void consumeInputStream(Consumer<InputStream> consumer) {
        try (DownloadHandler ignored = this) {
            consumer.accept(inputStream);
        }
    }

    /** 将不超过 20 MiB 的文件读入内存，较大文件应使用流式输出。 */
    public byte[] toByteArray() {
        return toByteArray(DEFAULT_ARRAY_LIMIT);
    }

    /** 按调用方显式指定的上限读入内存，超限时关闭流并抛出异常。 */
    public byte[] toByteArray(int maximumBytes) {
        try (DownloadHandler ignored = this; ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (maximumBytes < 0) {
                throw new FileStorageException("内存下载上限不能为负数");
            }
            byte[] buffer = new byte[8192];
            int count;
            long total = 0;
            while ((count = inputStream.read(buffer)) != -1) {
                total += count;
                if (total > maximumBytes) {
                    throw new FileStorageException("文件超过内存下载上限，请使用流式输出");
                }
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        } catch (IOException exception) {
            throw new FileStorageException("读取下载文件失败", exception);
        }
    }

    /** 流式写入目标，完成或失败后关闭输入，不关闭目标流。 */
    public void writeToOutputStream(OutputStream output) {
        try (DownloadHandler ignored = this) {
            inputStream.transferTo(output);
        } catch (IOException exception) {
            throw new FileStorageException("输出下载文件失败", exception);
        }
    }

    /** 显式释放尚未消费完毕的下载流。 */
    @Override
    public void close() {
        try {
            inputStream.close();
        } catch (IOException exception) {
            throw new FileStorageException("关闭下载文件失败", exception);
        }
    }
}
