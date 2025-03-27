package com.whim.file.handler;

import com.whim.common.exception.FileStorageException;

import java.io.ByteArrayOutputStream;
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
    // 输入流，用于下载数据
    private final InputStream inputStream;
    // 清理处理器，用于在输入流操作完成后执行清理操作
    private final Runnable closeHandler;

    private DownloadHandler(InputStream inputStream, Runnable closeHandler) {
        this.inputStream = inputStream;
        this.closeHandler = closeHandler;
    }

    /**
     * 创建DownloadHandler实例的工厂方法
     *
     * @param inputStream  输入流，用于下载数据
     * @param closeHandler 清理处理器，用于在输入流操作完成后执行清理操作
     * @return DownloadHandler实例
     */
    public static DownloadHandler of(InputStream inputStream, Runnable closeHandler) {
        return new DownloadHandler(inputStream, closeHandler);
    }

    /**
     * 使用输入流执行给定的操作
     *
     * @param consumer 消费者函数，用于处理输入流
     * @throws FileStorageException 如果流处理过程中发生错误
     */
    public void inputStream(Consumer<InputStream> consumer) {
        try (InputStream is = this.inputStream) {
            consumer.accept(is);
        } catch (IOException e) {
            throw new FileStorageException("流处理失败", e);
        } finally {
            this.closeResources();
        }
    }

    /**
     * 将输入流转换为字节数组
     *
     * @return 字节数组，包含输入流的数据
     * @throws FileStorageException 如果流读取过程中发生错误
     */
    public byte[] bytes() {
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

    /**
     * 将输入流的数据写入到输出流中
     *
     * @param outputStream 输出流，用于写入数据
     * @throws FileStorageException 如果流写入过程中发生错误
     */
    public void outputStream(OutputStream outputStream) {
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

    /**
     * 执行清理操作，调用初始化时设置的清理处理器
     */
    private void closeResources() {
        if (closeHandler != null) {
            closeHandler.run();
        }
    }

}
