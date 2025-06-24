package com.whim.file.client;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.whim.file.config.properties.FileStorageProperties.AliYunOssStorageProperties;


/**
 * @author jince
 * date: 2025/3/18 21:32
 * description: 阿里云oss文件存储客户端工厂
 */
@Getter
@Slf4j
public class AliyunOssFileStorageClientFactory extends BaseFileStorageClientFactory<OSS> {
    public AliyunOssFileStorageClientFactory(AliYunOssStorageProperties aliYunOssStorageProperties) {
        super(aliYunOssStorageProperties.getUrl(), aliYunOssStorageProperties.getAccessKey(), aliYunOssStorageProperties.getSecretKey(), aliYunOssStorageProperties.getRegion());
    }

    @Override
    public OSS getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    // 创建OSSClient实例。
                    ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
                    // 显式声明使用 V4 签名算法
                    clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
                    client = OSSClientBuilder.create()
                            .endpoint(url)
                            .region(region)
                            .credentialsProvider(new DefaultCredentialProvider(accessKey, secretKey))
                            .clientConfiguration(clientBuilderConfiguration)
                            .build();

                }
            }
        }
        return client;
    }

    @Override
    public void close() {
        if (client != null) {
            log.info("阿里云oss客户端工厂关闭了");
            client.shutdown();
        }
    }
}
