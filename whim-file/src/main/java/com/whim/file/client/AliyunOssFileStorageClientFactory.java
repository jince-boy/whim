package com.whim.file.client;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import lombok.Getter;

import static com.whim.file.config.FileStorageProperties.AliYunOssStorageProperties;


/**
 * @author jince
 * date: 2025/3/18 21:32
 * description: 阿里云oss文件存储客户端工厂
 */
@Getter
public class AliyunOssFileStorageClientFactory extends BaseFileStorageClientFactory<OSS> {
    public AliyunOssFileStorageClientFactory(AliYunOssStorageProperties aliYunOssStorageProperties) {
        super(aliYunOssStorageProperties.getName(), aliYunOssStorageProperties.getUrl(), aliYunOssStorageProperties.getAccessKey(), aliYunOssStorageProperties.getSecretKey(), aliYunOssStorageProperties.getBucket(), aliYunOssStorageProperties.getBasePath());
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
                            .credentialsProvider(new DefaultCredentialProvider(accessKey, secretKey))
                            .clientConfiguration(clientBuilderConfiguration)
                            .build();

                }
            }
        }
        return client;
    }

    @Override
    public void close() throws Exception {
        if (client != null) {
            client.shutdown();
        }
    }
}
