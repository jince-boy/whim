package com.whim.file.adapter;

import com.whim.file.adapter.wrapper.HttpServletRequestWrapper;
import com.whim.file.adapter.wrapper.IFileWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

/**
 * @author jince
 * date: 2025/3/12 19:56
 * description: httpServletRequest适配器
 */
@Component
@RequiredArgsConstructor
public class HttpServletRequestAdapter implements IFileAdapter {
    private final Tika tika;

    @Override
    public boolean isSupport(Object file) {
        return file instanceof HttpServletRequest;
    }

    @Override
    public IFileWrapper getFileWrapper(Object file) {
        HttpServletRequest request = (HttpServletRequest) file;
        return new HttpServletRequestWrapper(request, tika);
    }
}
