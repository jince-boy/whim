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
    protected String setFileName(File file) {
        return "";
    }

    @Override
    protected Long setFileSize(File file) {
        return 0L;
    }

    @Override
    protected String setFileContentType(File file) {
        return "";
    }

    @Override
    protected String setFileExtension(File file) {
        return "";
    }

    @Override
    protected InputStream setInputStream(File file) throws IOException {
        return null;
    }
}
