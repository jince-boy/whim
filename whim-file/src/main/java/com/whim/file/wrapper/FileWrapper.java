package com.whim.file.wrapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Jince
 * date: 2024/11/16 21:50
 * description:
 */
public class FileWrapper extends BaseFileWrapper<File>{

    public FileWrapper(File file) {
        super(file);
    }

    @Override
    protected String setFileName() {
        return "";
    }

    @Override
    protected Long setFileSize() {
        return 0L;
    }

    @Override
    protected String setFileContentType() {
        return "";
    }

    @Override
    protected String setFileExtension() {
        return "";
    }

    @Override
    protected InputStream setInputStream() throws IOException {
        return null;
    }
}
