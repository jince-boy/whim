package com.whim.file.handler;

import com.whim.core.exception.FileStorageException;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * @author jince
 * date: 2025/3/27 11:32
 * description: 下载处理器类，用于处理下载过程中的输入流
 * 它提供了一种机制来安全地操作输入流，并在操作完成后执行清理操作
 */
public class DownloadHandler {
    private final InputStream inputStream;
    private final Runnable closeHandler;

    private DownloadHandler(InputStream inputStream, Runnable closeHandler) {
        this.inputStream = inputStream;
        this.closeHandler = closeHandler;
    }

    public static DownloadHandler of(InputStream inputStream, Runnable closeHandler) {
        return new DownloadHandler(inputStream, closeHandler);
    }

    public void consumeInputStream(Consumer<InputStream> consumer) {
        try (InputStream is = this.inputStream) {
            consumer.accept(is);
        } catch (IOException e) {
            throw new FileStorageException("流处理失败", e);
        } finally {
            this.closeResources();
        }
    }

    public InputStream getInputStream() {
        return new FilterInputStream(inputStream) {
            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public void close() throws IOException {
                try {
                    inputStream.close();
                } finally {
                    closeResources();
                }
            }
        };
    }

    public byte[] toByteArray() {
        try (InputStream is = this.inputStream;
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new FileStorageException("字节转换失败", e);
        } finally {
            this.closeResources();
        }
    }

    public void writeToOutputStream(OutputStream outputStream) {
        try (InputStream is = this.inputStream) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new FileStorageException("流输出失败", e);
        } finally {
            this.closeResources();
        }
    }

    private void closeResources() {
        if (closeHandler != null) {
            closeHandler.run();
        }
    }
}

