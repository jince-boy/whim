package com.whim.file.adapter;

import com.whim.file.adapter.wrapper.IFileWrapper;
import com.whim.file.adapter.wrapper.URLWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import java.net.URL;

/**
 * @author jince
 * date: 2025/3/12 19:58
 * description: url适配器
 */
@Component
@RequiredArgsConstructor
public class URLAdapter implements IFileAdapter {
    private final Tika tika;

    @Override
    public boolean isSupport(Object file) {
        return file instanceof URL;
    }

    @Override
    public IFileWrapper getFileWrapper(Object file) {
        URL url = (URL) file;
        return new URLWrapper(url, tika);
    }
}
