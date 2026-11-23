CREATE TABLE sys_user_two_factor (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    encrypted_secret VARCHAR(512) NOT NULL COMMENT '加密的验证器密钥',
    last_step BIGINT NOT NULL COMMENT '已接受的最近动态码时间步',
    recovery_hash CHAR(64) NOT NULL COMMENT '一次性恢复码的 SHA-256 摘要',
    create_by BIGINT NULL,
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_by BIGINT NULL,
    update_time DATETIME(3) NULL,
    delete_by BIGINT NULL,
    delete_time DATETIME(3) NULL,
    deleted BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_two_factor (user_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户自行绑定的两步验证凭据';
