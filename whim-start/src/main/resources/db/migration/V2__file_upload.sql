-- 上传任务独立于部门数据范围，仅允许所属用户操作；每个任务绑定不可变对象键。
CREATE TABLE file_upload_owner (
    owner_id BIGINT NOT NULL,
    PRIMARY KEY (owner_id)
) ENGINE=InnoDB COMMENT='上传额度并发协调，每用户一行';

CREATE TABLE file_upload_task (
    id CHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    owner_id BIGINT NOT NULL,
    request_id CHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(150) NOT NULL,
    fingerprint CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    part_size BIGINT NOT NULL,
    part_count INT NOT NULL,
    platform VARCHAR(32) NOT NULL,
    config_name VARCHAR(100) NOT NULL,
    object_key VARCHAR(300) NOT NULL,
    upload_id VARCHAR(1024) NULL,
    status VARCHAR(20) NOT NULL,
    cleaned BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at DATETIME(6) NOT NULL,
    signature_expires_at DATETIME(6) NOT NULL,
    create_time DATETIME(6) NOT NULL,
    update_time DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_upload_owner_request (owner_id, request_id),
    KEY idx_upload_owner_created (owner_id, create_time),
    KEY idx_upload_owner_status (owner_id, status),
    KEY idx_upload_cleanup (cleaned, update_time),
    CONSTRAINT chk_upload_size CHECK (file_size > 0 AND part_size > 0 AND part_count BETWEEN 1 AND 10000),
    CONSTRAINT chk_upload_status CHECK (status IN ('CREATED','UPLOADING','COMPLETING','COMPLETED','CANCELING','CANCELED','EXPIRED','FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='个人文件及可恢复上传任务';

CREATE TABLE file_upload_part (
    id CHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    task_id CHAR(32) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    part_number INT NOT NULL,
    part_size BIGINT NOT NULL,
    content_md5 CHAR(24) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    content_sha256 CHAR(44) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_upload_task_part (task_id, part_number),
    CONSTRAINT fk_upload_part_task FOREIGN KEY (task_id) REFERENCES file_upload_task(id),
    CONSTRAINT chk_upload_part_size CHECK (part_size > 0 AND part_number BETWEEN 1 AND 10000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分片上传前固定的内容约束';

-- 独立文件页面，普通角色需显式授予对应权限；不赋予部门范围内的他人文件访问权。
INSERT INTO sys_permission
    (id, route_name, menu_name, perms, parent_id, menu_type, path, component, sort, built_in, create_by)
VALUES
    (1600, 'FileUpload', '我的文件', 'system:fileUpload:list', 0, 2, '/files', 'file/upload/index', 60, 1, 1),
    (1601, NULL, '上传文件', 'system:fileUpload:upload', 1600, 3, NULL, NULL, 1, 1, 1),
    (1602, NULL, '取消上传', 'system:fileUpload:cancel', 1600, 3, NULL, NULL, 2, 1, 1),
    (1603, NULL, '下载文件', 'system:fileUpload:download', 1600, 3, NULL, NULL, 3, 1, 1);

INSERT INTO sys_role_permission (id, role_id, permission_id, create_by)
VALUES (1600, 1, 1600, 1), (1601, 1, 1601, 1), (1602, 1, 1602, 1), (1603, 1, 1603, 1);

