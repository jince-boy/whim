package com.whim.file.adapter;

import com.whim.file.adapter.wrapper.IFileWrapper;
import com.whim.file.adapter.wrapper.URIWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;

/**
 * @author jince
 * date: 2025/3/12 19:58
 * description: url适配器
 */
@Component
@RequiredArgsConstructor
public class URIAdapter implements IFileAdapter {

    @Override
    public boolean isSupport(Object file) {
        return file instanceof URI;
    }

    @Override
    public IFileWrapper getFileWrapper(Object file) {
        URI uri = (URI) file;
        return new URIWrapper(uri);
    }
}
