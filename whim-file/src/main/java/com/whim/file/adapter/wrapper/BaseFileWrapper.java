package com.whim.file.adapter.wrapper;

import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;

import java.io.InputStream;

/**
 * @author jince
 * date: 2025/2/17 21:41
 * description: 文件包装器基类
 */
@RequiredArgsConstructor
public abstract class BaseFileWrapper<T> implements IFileWrapper {
    protected final T file;
    protected final Tika tika;
    protected InputStream inputStream;
}
