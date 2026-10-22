package com.whim.file.handler;

import lombok.Getter;

/**
 * @author Jince
 * @date 2026/10/22
 * @description 文件操作选项；在配置函数执行完毕后统一解析默认值。
 */
@Getter
public final class FileHandler {
    private final String platform;
    private final String platformConfigName;
    private final String objectKey;
    private final String fileName;
    private final String storagePath;
    private final String contentType;

    /** 创建本次操作的不可变选项。 */
    private FileHandler(Builder builder) {
        platform = builder.platform;
        platformConfigName = builder.platformConfigName;
        objectKey = builder.objectKey;
        fileName = builder.fileName;
        storagePath = builder.storagePath;
        contentType = builder.contentType;
    }

    /**
     * @author Jince
     * @date 2026/10/22
     * @description 文件操作选项构建器，不在设置选项时访问存储。
     */
    public static final class Builder {
        private String platform;
        private String platformConfigName;
        private String objectKey;
        private String fileName;
        private String storagePath;
        private String contentType;

        /** 指定存储平台。 */
        public Builder platform(String value) {
            platform = value;
            return this;
        }

        /** 指定该平台下的配置名称。 */
        public Builder platformConfigName(String value) {
            platformConfigName = value;
            return this;
        }

        /** 指定完整相对 key，不可与目录或文件名同时设置。 */
        public Builder objectKey(String value) {
            objectKey = value;
            return this;
        }

        /** 指定不含目录的存储文件名。 */
        public Builder fileName(String value) {
            fileName = value;
            return this;
        }

        /** 指定相对目录，使用正斜杠分隔。 */
        public Builder storagePath(String value) {
            storagePath = value;
            return this;
        }

        /** 指定内容类型，不作为文件内容安全校验。 */
        public Builder contentType(String value) {
            contentType = value;
            return this;
        }

        /** 生成操作选项。 */
        public FileHandler build() {
            return new FileHandler(this);
        }
    }
}
