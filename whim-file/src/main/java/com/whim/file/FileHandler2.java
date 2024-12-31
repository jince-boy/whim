package com.whim.file;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Jince
 * date: 2024/11/14 23:08
 * description: 文件处理器
 */
@Slf4j
@Getter
public class FileHandler2 {


//    private final FileStorageProperties fileStorageProperties;
//    private final Map<String, IFileStorage> allFileStorage;
//    private final List<IFileAdapter> allFileAdapter;
//    /**
//     * 文件包装器
//     */
//    private IFileWrapper wrapper;
//    private IFileStorage fileStorage;
//    private final FileInfo fileInfo;
//
//    /**
//     * 构造函数
//     *
//     * @param allFileStorage 全部存储方式
//     */
//    public FileHandler(FileStorageProperties fileStorageProperties, Map<String, IFileStorage> allFileStorage, List<IFileAdapter> allFileAdapter) {
//        this.fileStorageProperties = fileStorageProperties;
//        this.allFileStorage = allFileStorage;
//        this.allFileAdapter = allFileAdapter;
//        this.fileStorage = allFileStorage.get(fileStorageProperties.getDefaultStorage());
//        this.fileInfo = new FileInfo();
//    }
//
//    /**
//     * 包装文件
//     *
//     * @param file 文件
//     * @return FileHandler 文件处理器
//     */
//    public FileHandler warpFile(Object file) {
//        if (Objects.isNull(file)) {
//            throw new FileStorageException("文件为空");
//        }
//        for (IFileAdapter adapter : allFileAdapter) {
//            if (adapter.isSupport(file)) {
//                this.wrapper = adapter.getFileWrapper(file);
//                return this;
//            }
//        }
//        throw new FileStorageException("没有找到支持的适配器");
//    }
//
//    /**
//     * 设置文件名称
//     *
//     * @param fileName 文件名称
//     * @return FileHandler 文件处理器
//     */
//    public FileHandler setFileName(String fileName) {
//        if (fileName == null || fileName.trim().isBlank()) {
//            throw new FileStorageException("文件名称不能为空");
//        }
//        if (fileName.matches("^[a-zA-Z0-9_-]{1,255}$")) {
//            this.fileInfo.setFileName(fileName);
//            return this;
//        }
//        throw new FileStorageException("文件名称格式不正确");
//    }
//
//    /**
//     * 设置存储路径
//     *
//     * @param storagePath 存储路径
//     * @return FileHandler 文件处理器
//     */
//    public FileHandler setPath(String storagePath) {
//        if (storagePath == null || storagePath.trim().isBlank()) {
//            throw new FileStorageException("文件路径不能为空");
//        }
//        this.fileInfo.setPath(storagePath);
//        return this;
//    }
//
//    /**
//     * 设置存储平台
//     *
//     * @param platform 存储平台
//     * @return FileHandler 文件处理器
//     */
//    public FileHandler setPlatform(String platform) {
//        IFileStorage iFileStorage = allFileStorage.get(platform);
//        if (iFileStorage == null) {
//            throw new FileStorageException("未找到对应的存储平台");
//        }
//        this.fileStorage = iFileStorage;
//        this.fileInfo.setPlatform(platform);
//        return this;
//    }
//
//    public FileHandler image(Consumer<Thumbnails.Builder<? extends InputStream>> consumer) {
//        if (this.wrapper.getFileContentType().startsWith("image/")) {
//            try {
//                InputStream inputStream = this.wrapper.getInputStream();
//                Thumbnails.Builder<? extends InputStream> builder = Thumbnails.of(inputStream);
//                consumer.accept(builder);
//                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//                builder.toOutputStream(outputStream);
//                InputStream processedInputStream = new ByteArrayInputStream(outputStream.toByteArray());
//                this.wrapper.setFileSize((long) outputStream.size());
//                this.wrapper.setInputStream(processedInputStream);
//            } catch (IOException e) {
//                throw new FileStorageException("图片处理失败", e);
//            }
//        } else {
//            throw new FileStorageException("不是图片类型的对象，无法处理进行图像处理");
//        }
//        return this;
//    }
//
//
//    /**
//     * 给FileInfo填充基本信息
//     */
//    private void fillFileInfo() {
//        // 设置基本属性
//        this.fileInfo.setOriginalFileName(this.wrapper.getFileName());
//        this.fileInfo.setFileSize(FileUtil.formatFileSize(this.wrapper.getFileSize()));
//        this.fileInfo.setExtension(this.wrapper.getFileExtension());
//        this.fileInfo.setContentType(this.wrapper.getFileContentType());
//        this.fileInfo.setPlatform(Objects.requireNonNullElse(this.fileInfo.getPlatform(), this.fileStorageProperties.getDefaultStorage()));
//        this.fileInfo.setPath(Paths.get(StringUtils.defaultIfEmpty(StringUtils.strip(fileInfo.getPath(), "/"), "")).toString());
//        this.fileInfo.setFileName(Objects.requireNonNullElse(this.fileInfo.getFileName(), this.wrapper.getFileName()));
//    }
//
//    /**
//     * 上传文件
//     *
//     * @return FileInfo 上传文件信息
//     */
//    public FileInfo upload() {
//        this.fillFileInfo();
//        if (this.fileStorage.upload(this)) {
//            log.info(this.fileInfo.toString());
//            return this.fileInfo;
//        } else {
//            return null;
//        }
//    }
}
