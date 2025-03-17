package com.whim.file.adapter.wrapper;

import com.whim.common.exception.FileStorageException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author jince
 * date: 2025/3/12 19:41
 * description: HttpServletRequest的包装类
 */
public class HttpServletRequestWrapper extends BaseFileWrapper<HttpServletRequest> {

    public HttpServletRequestWrapper(HttpServletRequest file) {
        super(file);
    }

    @Override
    public String getExtension() {
        String contentDisposition = file.getHeader("Content-Disposition");
        if (contentDisposition != null && contentDisposition.contains("filename=")) {
            return FilenameUtils.getExtension(contentDisposition
                    .substring(contentDisposition.indexOf("filename=") + 9)
                    .replace("\"", ""));
        }
        return "";
    }

    @Override
    public InputStream getInputStream() {
        if (inputStream == null) {
            try {
                inputStream = new BufferedInputStream(file.getInputStream());
            } catch (IOException e) {
                throw new FileStorageException("无法获取HttpServletRequest的输入流", e);
            }
        }
        return inputStream;
    }

    @Override
    public void close() throws Exception {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
