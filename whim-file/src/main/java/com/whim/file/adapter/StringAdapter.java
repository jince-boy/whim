package com.whim.file.adapter;

import com.whim.file.adapter.wrapper.IFileWrapper;
import com.whim.file.adapter.wrapper.StringWrapper;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

/**
 * @author jince
 * date: 2025/3/12 19:57
 * description: String适配器
 */
@Component
@RequiredArgsConstructor
public class StringAdapter implements IFileAdapter {
    private final Tika tika;

    @Override
    public boolean isSupport(Object file) {
        return file instanceof String;
    }

    @Override
    public IFileWrapper getFileWrapper(Object file) {
        String stringContent = (String) file;
        return new StringWrapper(stringContent, tika);
    }
}