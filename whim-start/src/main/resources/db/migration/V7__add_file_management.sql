-- 文件管理独立配置，不占用 sys_config；平台能力复用 whim-file。
CREATE TABLE sys_file_storage (
    id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL COMMENT '配置名称',
    platform VARCHAR(32) NOT NULL COMMENT 'local/minio/aliyunOss',
    base_path VARCHAR(512) NOT NULL DEFAULT '' COMMENT '本地绝对目录或对象前缀',
    endpoint VARCHAR(512) NOT NULL DEFAULT '',
    bucket VARCHAR(128) NOT NULL DEFAULT '',
    region VARCHAR(64) NOT NULL DEFAULT '',
    access_key VARCHAR(256) NOT NULL DEFAULT '',
    secret_cipher TEXT NOT NULL COMMENT 'JWE密文，不返回客户端',
    default_flag TINYINT NULL COMMENT '1默认，其他为NULL',
    create_by BIGINT NULL,
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_by BIGINT NULL,
    update_time DATETIME(3) NULL,
    delete_by BIGINT NULL,
    delete_time DATETIME(3) NULL,
    deleted BIGINT NOT NULL DEFAULT 0 COMMENT '0未删除，删除后为主键ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_storage_name (name, deleted),
    UNIQUE KEY uk_file_storage_default (default_flag, deleted),
    CONSTRAINT chk_file_storage_default CHECK (default_flag IS NULL OR default_flag = 1),
    CONSTRAINT chk_file_storage_platform CHECK (platform IN ('local', 'minio', 'aliyunOss'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件存储配置';

CREATE TABLE sys_file (
    id BIGINT NOT NULL,
    storage_id BIGINT NOT NULL,
    storage_name VARCHAR(64) NOT NULL COMMENT '上传时的配置名称',
    platform VARCHAR(32) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    object_key VARCHAR(512) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    create_by BIGINT NULL,
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_by BIGINT NULL,
    update_time DATETIME(3) NULL,
    delete_by BIGINT NULL,
    delete_time DATETIME(3) NULL,
    deleted BIGINT NOT NULL DEFAULT 0 COMMENT '0未删除，删除后为主键ID',
    PRIMARY KEY (id),
    KEY idx_file_storage (storage_id, deleted, id),
    KEY idx_file_deleted (deleted, id),
    CONSTRAINT fk_file_storage FOREIGN KEY (storage_id) REFERENCES sys_file_storage (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件记录';

INSERT INTO sys_dict_type (id, name, type, remark, create_by)
VALUES (2005, '文件存储类型', 'sys_file_storage_platform', '文件模块支持的存储类型', 1);
INSERT INTO sys_dict_data (id, type, label, value, list_class, sort, create_by)
VALUES (20050, 'sys_file_storage_platform', '本地存储', 'local', 'default', 0, 1),
       (20051, 'sys_file_storage_platform', 'MinIO', 'minio', 'info', 1, 1),
       (20052, 'sys_file_storage_platform', '阿里云 OSS', 'aliyunOss', 'warning', 2, 1);

INSERT INTO sys_permission
    (id, route_name, menu_name, perms, parent_id, menu_type, path, component, sort, icon, built_in, create_by)
VALUES (1800, 'SysFile', '文件管理', 'system:file:page', 100, 2, '/system/file', 'system/file/index', 70, 'folder-open', 1, 1),
       (1801, NULL, '上传文件', 'system:file:upload', 1800, 3, NULL, NULL, 1, NULL, 1, 1),
       (1802, NULL, '下载文件', 'system:file:download', 1800, 3, NULL, NULL, 2, NULL, 1, 1),
       (1803, NULL, '删除文件', 'system:file:delete', 1800, 3, NULL, NULL, 3, NULL, 1, 1),
       (1804, NULL, '存储配置查询', 'system:file:storageList', 1800, 3, NULL, NULL, 4, NULL, 1, 1),
       (1805, NULL, '新增存储配置', 'system:file:addStorage', 1800, 3, NULL, NULL, 5, NULL, 1, 1),
       (1806, NULL, '修改存储配置', 'system:file:updateStorage', 1800, 3, NULL, NULL, 6, NULL, 1, 1),
       (1807, NULL, '切换默认存储', 'system:file:defaultStorage', 1800, 3, NULL, NULL, 7, NULL, 1, 1),
       (1808, NULL, '删除存储配置', 'system:file:deleteStorage', 1800, 3, NULL, NULL, 8, NULL, 1, 1);

INSERT INTO sys_role_permission (id, role_id, permission_id, create_by)
SELECT 180000 + id, 1, id, 1 FROM sys_permission WHERE id BETWEEN 1800 AND 1808;
