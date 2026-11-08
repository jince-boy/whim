package com.whim.system.model.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.whim.mybatisplus.model.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jince
 * @date 2026/11/08
 * @description 文件存储配置，不输出凭据。
 */
@Getter
@Setter
public class SysFileStorage extends BaseEntity {
    private Long id;
    private String name;
    private String platform;
    private String basePath;
    private String endpoint;
    private String bucket;
    private String region;
    private String accessKey;
    /** 仅保存认证加密后的密文，禁止返回给前端。 */
    private String secretCipher;
    /** 1 表示默认配置，其他配置为 null，配合唯一索引约束默认项。 */
    private Integer defaultFlag;
    @TableLogic
    private Long deleted;
}
