package com.whim.file.support;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 对实际读取量和传输总时间进行约束，防止未知长度输入绕过上传限制。
 */
public final class LimitedInputStream extends FilterInputStream {
    private final long maximumBytes;
    private final long deadline;
    private long consumed;
    private final AtomicBoolean closed = new AtomicBoolean();

    /** 返回实际读取的字节数。 */
    public long getBytesRead() {
        return consumed;
    }

    /** 包装输入流；deadline 使用 System.nanoTime 的时间基准。 */
    public LimitedInputStream(InputStream input, long maximumBytes, long deadline) {
        super(input);
        this.maximumBytes = maximumBytes;
        this.deadline = deadline;
    }

    /** 检查读取期限和累计大小。 */
    private void checkLimit() throws IOException {
        if (closed.get()) {
            throw new IOException("文件输入流已经关闭");
        }
        if (System.nanoTime() - deadline >= 0) {
            throw new SocketTimeoutException("文件传输超过允许时间");
        }
        if (consumed > maximumBytes) {
            throw new IOException("文件大小超过允许上限");
        }
    }

    /** 读取一个字节并累计实际大小。 */
    @Override
    public int read() throws IOException {
        checkLimit();
        int value = in.read();
        if (value >= 0) {
            consumed++;
        }
        checkLimit();
        return value;
    }

    /** 最多额外读取一个字节以识别超限输入。 */
    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, buffer.length);
        checkLimit();
        int allowed = (int) Math.min(length, maximumBytes - consumed + 1);
        int count = in.read(buffer, offset, allowed);
        if (count > 0) {
            consumed += count;
        }
        checkLimit();
        return count;
    }

    /** 跳过操作同样计入读取限制。 */
    @Override
    public long skip(long count) throws IOException {
        long remaining = Math.max(0, count);
        byte[] buffer = new byte[8192];
        while (remaining > 0) {
            int read = read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if (read < 0) {
                break;
            }
            remaining -= read;
        }
        return Math.max(0, count) - remaining;
    }

    /** 输入不可回退，避免重复读取绕过计数。 */
    @Override
    public boolean markSupported() {
        return false;
    }

    /** 不保存输入流位置。 */
    @Override
    public void mark(int readLimit) {
        // 本包装器不支持回退。
    }

    /** 拒绝回退输入流。 */
    @Override
    public void reset() throws IOException {
        throw new IOException("文件输入流不支持回退");
    }

    /** SDK 与上层服务都可能关闭输入，确保底层资源只关闭一次。 */
    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            super.close();
        }
    }
}
