package com.whim.file.support;

import com.whim.core.exception.FileStorageException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.imaging.Imaging;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author Jince
 * @date 2026/11/09
 * @description 根据文件实际内容生成 PNG 缩略图，限制读取量、解码像素及并发内存占用。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageThumbnailGenerator {
    public static final int MAXIMUM_SOURCE_BYTES = 20 * 1024 * 1024;
    private static final long MAXIMUM_SOURCE_PIXELS = 24_000_000L;
    private static final Semaphore GENERATION_PERMITS = new Semaphore(2);

    /**
     * 等比生成指定边界内的 PNG 缩略图，并关闭输入流；不支持或超出预览限制时返回空数组。
     *
     * @param source 待预览文件的输入流
     * @param width 缩略图最大宽度，范围为 1 至 512
     * @param height 缩略图最大高度，范围为 1 至 512
     * @return PNG 图片内容，不可预览时为空数组
     */
    public static byte[] generateThumbnail(InputStream source, int width, int height) {
        boolean acquired = false;
        try (InputStream input = new LimitedInputStream(source, MAXIMUM_SOURCE_BYTES + 1L,
                System.nanoTime() + Duration.ofSeconds(30).toNanos())) {
            if (width < 1 || height < 1 || width > 512 || height > 512) {
                throw new FileStorageException("缩略图尺寸必须在1至512像素之间");
            }
            // 限制同时解码的图片数量，避免列表并发预览消耗过多堆内存。
            acquired = GENERATION_PERMITS.tryAcquire(10, TimeUnit.SECONDS);
            if (!acquired) {
                throw new FileStorageException("缩略图生成繁忙，请稍后重试");
            }
            byte[] content = input.readNBytes(MAXIMUM_SOURCE_BYTES + 1);
            if (content.length == 0 || content.length > MAXIMUM_SOURCE_BYTES) {
                return new byte[0];
            }
            try {
                // 先读取真实尺寸再解码，不信任上传时的 MIME 或扩展名，拦截压缩体积小但像素巨大的图片。
                Dimension dimensions = Imaging.getImageSize(content);
                if (dimensions.width < 1 || dimensions.height < 1
                        || dimensions.width > 16384 || dimensions.height > 16384
                        || (long) dimensions.width * dimensions.height > MAXIMUM_SOURCE_PIXELS) {
                    return new byte[0];
                }
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                Thumbnails.of(new ByteArrayInputStream(content))
                        .size(width, height)
                        .useExifOrientation(true)
                        .outputFormat("png")
                        .toOutputStream(output);
                return output.toByteArray();
            } catch (IOException exception) {
                // 不支持的格式或损坏的图片只影响预览，原文件仍可正常下载。
                return new byte[0];
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new FileStorageException("缩略图生成已中断", exception);
        } catch (IOException exception) {
            throw new FileStorageException("读取缩略图源文件失败", exception);
        } finally {
            if (acquired) {
                GENERATION_PERMITS.release();
            }
        }
    }
}
