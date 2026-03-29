package com.whim.web.wrapper;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author Jince
 * @date 2026/03/29
 * @description 可重复读取请求体的请求包装器
 */
public class RepeatableReadHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    public RepeatableReadHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedBodyServletInputStream(this.cachedBody);
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(this.cachedBody), StandardCharsets.UTF_8));
    }

    @Override
    public int getContentLength() {
        return this.cachedBody.length;
    }

    @Override
    public long getContentLengthLong() {
        return this.cachedBody.length;
    }

    private static final class CachedBodyServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream inputStream;
        private ReadListener readListener;
        private boolean allDataReadNotified;

        private CachedBodyServletInputStream(byte[] cachedBody) {
            this.inputStream = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public int read() throws IOException {
            int read = this.inputStream.read();
            notifyIfAllDataRead();
            return read;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = this.inputStream.read(b, off, len);
            notifyIfAllDataRead();
            return read;
        }

        @Override
        public boolean isFinished() {
            return this.inputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            if (readListener == null) {
                throw new IllegalArgumentException("ReadListener must not be null");
            }
            this.readListener = readListener;
            try {
                if (!isFinished()) {
                    readListener.onDataAvailable();
                }
                notifyIfAllDataRead();
            } catch (IOException exception) {
                readListener.onError(exception);
            }
        }

        private void notifyIfAllDataRead() {
            if (this.readListener == null || this.allDataReadNotified || !isFinished()) {
                return;
            }
            this.allDataReadNotified = true;
            try {
                this.readListener.onAllDataRead();
            } catch (IOException exception) {
                this.readListener.onError(exception);
            }
        }
    }
}
