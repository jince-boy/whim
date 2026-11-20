-- Whim 空库初始化，适用 MySQL 8.0.12 及以上版本。
-- 主键由 MyBatis-Plus ASSIGN_ID 生成，业务实体逻辑删除，关联记录物理删除。
SET NAMES utf8mb4;

CREATE TABLE sys_dept (
    id BIGINT NOT NULL COMMENT '主键ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父部门ID，0表示根节点',
    ancestors TEXT NOT NULL COMMENT '祖级部门ID路径，逗号分隔',
    dept_name VARCHAR(64) NOT NULL COMMENT '部门名称',
    dept_code VARCHAR(64) NOT NULL COMMENT '部门编码',
    leader_user_id BIGINT NULL COMMENT '负责人用户ID',
    phone VARCHAR(20) NULL COMMENT '联系电话',
    email VARCHAR(64) NULL COMMENT '邮箱',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0正常，1停用',
    remark VARCHAR(255) NULL COMMENT '备注',
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    deleted BIGINT NOT NULL DEFAULT 0 COMMENT '删除标识：0未删除，已删除时为本行主键ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dept_parent_name (parent_id, dept_name, deleted)
        COMMENT '未删除的同级部门名称唯一',
    UNIQUE KEY uk_dept_code (dept_code, deleted)
        COMMENT '未删除的部门编码唯一',
    KEY idx_dept_parent (parent_id, deleted, status),
    KEY idx_dept_leader (leader_user_id, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

CREATE TABLE sys_user (
    id BIGINT NOT NULL COMMENT '主键ID',
    username VARCHAR(32) NOT NULL COMMENT '用户名，统一小写且删除后不可重复使用',
    super_administrator TINYINT NOT NULL DEFAULT 0 COMMENT '超级管理员身份：0否、1是',
    password VARCHAR(128) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '密码哈希值',
    avatar VARCHAR(255) NULL COMMENT '头像URL',
    name VARCHAR(24) NOT NULL COMMENT '真实姓名',
    email VARCHAR(64) NULL COMMENT '邮箱',
    phone VARCHAR(20) NULL COMMENT '手机号码',
    gender TINYINT NOT NULL DEFAULT 0 COMMENT '性别字典编码',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0正常，1停用',
    dept_id BIGINT NULL COMMENT '主部门ID，空表示未分配部门',
    remark VARCHAR(255) NULL COMMENT '备注',
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    deleted BIGINT NOT NULL DEFAULT 0 COMMENT '删除标识：0未删除，已删除时为本行主键ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username),
    KEY idx_user_dept (dept_id, deleted, status),
    CONSTRAINT chk_sys_user_super_administrator CHECK (super_administrator IN (0, 1)),
    CONSTRAINT fk_user_dept FOREIGN KEY (dept_id) REFERENCES sys_dept (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

CREATE TABLE sys_role (
    id BIGINT NOT NULL COMMENT '主键ID',
    role_name VARCHAR(32) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(128) NOT NULL COMMENT '角色编码',
    data_scope TINYINT NOT NULL DEFAULT 5 COMMENT '数据范围：1全部，2自定义部门，3本部门，4本部门及下级，5本人',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0正常，1停用',
    remark VARCHAR(255) NULL COMMENT '备注',
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    deleted BIGINT NOT NULL DEFAULT 0 COMMENT '删除标识：0未删除，已删除时为本行主键ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code, deleted)
        COMMENT '未删除的角色编码唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

CREATE TABLE sys_permission (
    id BIGINT NOT NULL COMMENT '主键ID',
    route_name VARCHAR(128) NULL COMMENT '路由名称',
    menu_name VARCHAR(64) NOT NULL COMMENT '菜单名称',
    perms VARCHAR(128) NULL COMMENT '操作权限码，可空；目录和外链不设置',
    built_in TINYINT NOT NULL DEFAULT 0 COMMENT '内置标志：0否，1是；内置定义不能改变授权语义，非核心节点允许删除',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID，0表示顶级菜单',
    menu_type TINYINT NOT NULL COMMENT '菜单类型：1目录，2菜单，3按钮，4外链',
    path VARCHAR(255) NULL COMMENT '路由路径或外链地址',
    param VARCHAR(255) NULL COMMENT '路由参数',
    component VARCHAR(255) NULL COMMENT '前端组件路径',
    is_cache TINYINT NOT NULL DEFAULT 0 COMMENT '是否缓存：0不缓存，1缓存',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
    icon VARCHAR(128) NULL COMMENT '菜单图标',
    visible TINYINT NOT NULL DEFAULT 0 COMMENT '显示状态：0显示，1隐藏',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0正常，1停用',
    remark VARCHAR(255) NULL COMMENT '备注',
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    deleted BIGINT NOT NULL DEFAULT 0 COMMENT '删除标识：0未删除，已删除时为本行主键ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_perms (perms),
    UNIQUE KEY uk_menu_route (route_name, deleted)
        COMMENT '未删除的非空路由名称唯一',
    UNIQUE KEY uk_menu_path (path, deleted)
        COMMENT '未删除的非空路径唯一',
    KEY idx_permission_parent (parent_id, deleted, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单权限表';

CREATE TABLE sys_post (
    id BIGINT NOT NULL COMMENT '主键ID',
    dept_id BIGINT NULL COMMENT '所属部门ID，空表示全组织岗位',
    post_name VARCHAR(64) NOT NULL COMMENT '岗位名称',
    post_code VARCHAR(64) NOT NULL COMMENT '岗位编码',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0正常，1停用',
    remark VARCHAR(255) NULL COMMENT '备注',
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    deleted BIGINT NOT NULL DEFAULT 0 COMMENT '删除标识：0未删除，已删除时为本行主键ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_code (post_code, deleted)
        COMMENT '未删除的岗位编码唯一',
    KEY idx_post_dept (dept_id, deleted, status),
    KEY idx_post_creator (create_by, deleted),
    CONSTRAINT fk_post_dept FOREIGN KEY (dept_id) REFERENCES sys_dept (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位表';

CREATE TABLE sys_config (
    id BIGINT NOT NULL COMMENT '主键ID',
    config_name VARCHAR(128) NOT NULL COMMENT '配置名称',
    config_key VARCHAR(128) NOT NULL COMMENT '配置键',
    value_type ENUM('TEXT', 'RADIO', 'JSON') NOT NULL DEFAULT 'TEXT' COMMENT '值类型',
    value_options JSON NULL COMMENT '单选项名称及存储值',
    config_value LONGTEXT NULL COMMENT '配置值',
    remark VARCHAR(255) NULL COMMENT '备注',
    built_in TINYINT NOT NULL DEFAULT 0 COMMENT '内置配置标志',
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    deleted BIGINT NOT NULL DEFAULT 0 COMMENT '删除标识：0未删除，已删除时为本行主键ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key_deleted (config_key, deleted),
    KEY idx_config_category (built_in, deleted, id),
    CONSTRAINT chk_config_built_in CHECK (built_in IN (0, 1) AND (built_in = 0 OR deleted = 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置表';

CREATE TABLE sys_dict_type (
    id BIGINT NOT NULL COMMENT '主键ID',
    name VARCHAR(64) NOT NULL COMMENT '字典名称',
    type VARCHAR(128) NOT NULL COMMENT '字典类型编码',
    remark VARCHAR(255) NULL COMMENT '备注',
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    deleted BIGINT NOT NULL DEFAULT 0 COMMENT '删除标识：0未删除，已删除时为本行主键ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_type_type (type, deleted)
        COMMENT '未删除的字典类型编码唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统字典类型表';

CREATE TABLE sys_dict_data (
    id BIGINT NOT NULL COMMENT '主键ID',
    type VARCHAR(128) NOT NULL COMMENT '所属字典类型编码',
    label VARCHAR(128) NOT NULL COMMENT '字典标签',
    value VARCHAR(128) NOT NULL COMMENT '字典值',
    list_class VARCHAR(16) NOT NULL DEFAULT 'default' COMMENT '回显样式：default、primary、info、success、warning、error',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序值',
    remark VARCHAR(255) NULL COMMENT '备注',
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    deleted BIGINT NOT NULL DEFAULT 0 COMMENT '删除标识：0未删除，已删除时为本行主键ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_data_type_value (type, value, deleted)
        COMMENT '同一类型下未删除的字典值唯一',
    KEY idx_dict_data_type (type, deleted, sort, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统字典数据表';

CREATE TABLE sys_user_role (
    id BIGINT NOT NULL COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_role_role (role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

CREATE TABLE sys_user_post (
    id BIGINT NOT NULL COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    post_id BIGINT NOT NULL COMMENT '岗位ID',
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_post (user_id, post_id),
    KEY idx_user_post_post (post_id),
    CONSTRAINT fk_user_post_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_user_post_post FOREIGN KEY (post_id) REFERENCES sys_post (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户岗位关联表';

CREATE TABLE sys_role_permission (
    id BIGINT NOT NULL COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    KEY idx_role_permission_permission (permission_id),
    CONSTRAINT fk_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色权限关联表';

CREATE TABLE sys_role_dept (
    id BIGINT NOT NULL COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_dept (role_id, dept_id),
    KEY idx_role_dept_dept (dept_id),
    CONSTRAINT fk_role_dept_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
    CONSTRAINT fk_role_dept_dept FOREIGN KEY (dept_id) REFERENCES sys_dept (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色部门关联表';

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
    upload_source VARCHAR(64) NOT NULL DEFAULT 'fileManagement' COMMENT '上传途径，关联sys_file_upload_source字典',
    file_name VARCHAR(512) NOT NULL COMMENT '存储文件名称，不含目录',
    original_name VARCHAR(255) NOT NULL,
    file_suffix VARCHAR(255) NOT NULL DEFAULT '' COMMENT '原文件后缀，不含点号',
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
    KEY idx_file_suffix (file_suffix, deleted, id),
    KEY idx_file_create_time (deleted, create_time, id),
    KEY idx_file_upload_source (upload_source, deleted, id),
    CONSTRAINT fk_file_storage FOREIGN KEY (storage_id) REFERENCES sys_file_storage (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='文件记录';

CREATE TABLE sys_file_upload_task (
    id BIGINT NOT NULL,
    request_id CHAR(32) NOT NULL COMMENT '客户端幂等请求标识',
    storage_id BIGINT NOT NULL,
    object_key VARCHAR(512) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    upload_source VARCHAR(64) NOT NULL,
    upload_id VARCHAR(2048) NULL COMMENT '存储端会话标识，不返回客户端',
    part_size INT NOT NULL,
    file_id BIGINT NULL COMMENT '已完成的文件记录，保证合并重试幂等',
    cancelled TINYINT NOT NULL DEFAULT 0,
    expires_at DATETIME(3) NOT NULL,
    create_by BIGINT NULL,
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_by BIGINT NULL,
    update_time DATETIME(3) NULL,
    delete_by BIGINT NULL,
    delete_time DATETIME(3) NULL,
    deleted BIGINT NOT NULL DEFAULT 0 COMMENT '0未删除，删除后为主键ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_upload_request (create_by, request_id, deleted),
    KEY idx_file_upload_expiry (deleted, expires_at),
    KEY idx_file_upload_storage (storage_id, deleted, cancelled, file_id),
    CONSTRAINT fk_file_upload_storage FOREIGN KEY (storage_id) REFERENCES sys_file_storage (id),
    CONSTRAINT chk_file_upload_size CHECK (file_size > 0 AND part_size > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='可恢复文件上传任务';

CREATE TABLE sys_login_log (
    id BIGINT NOT NULL,
    username VARCHAR(32) NOT NULL COMMENT '提交登录的用户名',
    ip_address VARCHAR(45) NULL,
    login_location VARCHAR(255) NULL,
    browser VARCHAR(128) NULL,
    os VARCHAR(128) NULL,
    device_type VARCHAR(64) NULL,
    status TINYINT NOT NULL COMMENT '0成功，1失败',
    remark VARCHAR(255) NOT NULL COMMENT '认证结果说明',
    login_time DATETIME NOT NULL COMMENT '本次认证结果确定的时间',
    create_by BIGINT NULL,
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_by BIGINT NULL,
    update_time DATETIME(3) NULL,
    delete_by BIGINT NULL,
    delete_time DATETIME(3) NULL,
    deleted BIGINT NOT NULL DEFAULT 0 COMMENT '0未删除，删除后为主键ID',
    PRIMARY KEY (id),
    KEY idx_login_log_deleted_id (deleted, id),
    KEY idx_login_log_time (deleted, login_time, id),
    KEY idx_login_log_username (username, deleted, login_time, id),
    KEY idx_login_log_ip (ip_address, deleted, login_time, id),
    CONSTRAINT chk_login_log_status CHECK (status IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统登录日志';

CREATE TABLE sys_oper_log (
    id BIGINT NOT NULL,
    title VARCHAR(64) NOT NULL,
    log_type TINYINT NOT NULL,
    method_name VARCHAR(255) NULL,
    request_method VARCHAR(16) NULL,
    oper_name VARCHAR(32) NULL,
    oper_ip VARCHAR(45) NULL,
    oper_location VARCHAR(255) NULL,
    request_url VARCHAR(512) NULL,
    request_param TEXT NULL COMMENT '允许记录的业务字段',
    response_param TEXT NULL COMMENT '响应状态摘要',
    status TINYINT NOT NULL COMMENT '0成功，1失败',
    error_message VARCHAR(512) NULL,
    oper_time DATETIME NOT NULL,
    cost_time BIGINT NOT NULL COMMENT '耗时（毫秒）',
    create_by BIGINT NULL,
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_by BIGINT NULL,
    update_time DATETIME(3) NULL,
    delete_by BIGINT NULL,
    delete_time DATETIME(3) NULL,
    deleted BIGINT NOT NULL DEFAULT 0 COMMENT '0未删除，删除后为主键ID',
    PRIMARY KEY (id),
    KEY idx_oper_log_deleted_id (deleted, id),
    KEY idx_oper_log_time (deleted, oper_time, id),
    KEY idx_oper_log_name (oper_name, deleted, oper_time, id),
    CONSTRAINT chk_oper_log_status CHECK (status IN (0, 1)),
    CONSTRAINT chk_oper_log_type CHECK (log_type BETWEEN 0 AND 6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统操作日志';

-- MySQL 8.0.12 通过触发器校验基础业务表的字段取值和逻辑删除标识。
DELIMITER $$

-- 校验新增部门的业务字段和删除标识。
CREATE TRIGGER trg_sys_dept_validate_insert BEFORE INSERT ON sys_dept
FOR EACH ROW
BEGIN
    IF NOT (NEW.status IN (0, 1) AND NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '部门状态无效或删除标识不是0或本行主键ID';
    END IF;
END$$

-- 校验修改部门的业务字段和删除标识。
CREATE TRIGGER trg_sys_dept_validate_update BEFORE UPDATE ON sys_dept
FOR EACH ROW
BEGIN
    IF NOT (NEW.status IN (0, 1) AND NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '部门状态无效或删除标识不是0或本行主键ID';
    END IF;
END$$

-- 校验新增用户的业务字段和删除标识。
CREATE TRIGGER trg_sys_user_validate_insert BEFORE INSERT ON sys_user
FOR EACH ROW
BEGIN
    IF NOT (NEW.status IN (0, 1) AND NEW.gender BETWEEN 0 AND 2 AND NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '用户状态、性别无效或删除标识不是0或本行主键ID';
    END IF;
END$$

-- 校验修改用户的业务字段和删除标识。
CREATE TRIGGER trg_sys_user_validate_update BEFORE UPDATE ON sys_user
FOR EACH ROW
BEGIN
    IF NOT (NEW.status IN (0, 1) AND NEW.gender BETWEEN 0 AND 2 AND NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '用户状态、性别无效或删除标识不是0或本行主键ID';
    END IF;
END$$

-- 校验新增角色的业务字段和删除标识。
CREATE TRIGGER trg_sys_role_validate_insert BEFORE INSERT ON sys_role
FOR EACH ROW
BEGIN
    IF NOT (NEW.status IN (0, 1) AND NEW.data_scope BETWEEN 1 AND 5 AND NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '角色状态、数据范围无效或删除标识不是0或本行主键ID';
    END IF;
END$$

-- 校验修改角色的业务字段和删除标识。
CREATE TRIGGER trg_sys_role_validate_update BEFORE UPDATE ON sys_role
FOR EACH ROW
BEGIN
    IF NOT (NEW.status IN (0, 1) AND NEW.data_scope BETWEEN 1 AND 5 AND NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '角色状态、数据范围无效或删除标识不是0或本行主键ID';
    END IF;
END$$

-- 校验新增菜单权限的业务字段和删除标识。
CREATE TRIGGER trg_sys_permission_validate_insert BEFORE INSERT ON sys_permission
FOR EACH ROW
BEGIN
    IF NOT (NEW.status IN (0, 1) AND NEW.menu_type BETWEEN 1 AND 4
            AND NEW.built_in IN (0, 1) AND NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '菜单状态、类型、内置标志或删除标识无效';
    END IF;
    IF NEW.menu_type IN (1, 4) AND NEW.perms IS NOT NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '目录和外链不能设置权限码';
    END IF;
END$$

-- 校验修改菜单权限的业务字段和删除标识。
CREATE TRIGGER trg_sys_permission_validate_update BEFORE UPDATE ON sys_permission
FOR EACH ROW
BEGIN
    IF NOT (NEW.status IN (0, 1) AND NEW.menu_type BETWEEN 1 AND 4
            AND NEW.built_in IN (0, 1) AND NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '菜单状态、类型、内置标志或删除标识无效';
    END IF;
    IF NEW.menu_type IN (1, 4) AND NEW.perms IS NOT NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '目录和外链不能设置权限码';
    END IF;
END$$

-- 校验新增岗位的业务字段和删除标识。
CREATE TRIGGER trg_sys_post_validate_insert BEFORE INSERT ON sys_post
FOR EACH ROW
BEGIN
    IF NOT (NEW.status IN (0, 1) AND NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '岗位状态无效或删除标识不是0或本行主键ID';
    END IF;
END$$

-- 校验修改岗位的业务字段和删除标识。
CREATE TRIGGER trg_sys_post_validate_update BEFORE UPDATE ON sys_post
FOR EACH ROW
BEGIN
    IF NOT (NEW.status IN (0, 1) AND NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '岗位状态无效或删除标识不是0或本行主键ID';
    END IF;
END$$

-- 校验新增配置的业务字段和删除标识。
CREATE TRIGGER trg_sys_config_validate_insert BEFORE INSERT ON sys_config
FOR EACH ROW
BEGIN
    IF NOT (NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '配置删除标识只能为0或本行主键ID';
    END IF;
END$$

-- 校验修改配置的业务字段和删除标识。
CREATE TRIGGER trg_sys_config_validate_update BEFORE UPDATE ON sys_config
FOR EACH ROW
BEGIN
    IF NOT (NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '配置删除标识只能为0或本行主键ID';
    END IF;
END$$

-- 校验新增字典类型的删除标识。
CREATE TRIGGER trg_sys_dict_type_validate_insert BEFORE INSERT ON sys_dict_type
FOR EACH ROW
BEGIN
    IF NOT (NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '字典类型删除标识只能为0或本行主键ID';
    END IF;
END$$

-- 校验修改字典类型的删除标识。
CREATE TRIGGER trg_sys_dict_type_validate_update BEFORE UPDATE ON sys_dict_type
FOR EACH ROW
BEGIN
    IF NOT (NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '字典类型删除标识只能为0或本行主键ID';
    END IF;
END$$

-- 校验新增字典数据的删除标识和回显样式。
CREATE TRIGGER trg_sys_dict_data_validate_insert BEFORE INSERT ON sys_dict_data
FOR EACH ROW
BEGIN
    IF NOT (NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '字典数据删除标识只能为0或本行主键ID';
    END IF;
    IF NEW.list_class NOT IN ('default', 'primary', 'info', 'success', 'warning', 'error') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '字典数据回显样式无效';
    END IF;
END$$

-- 校验修改字典数据的删除标识和回显样式。
CREATE TRIGGER trg_sys_dict_data_validate_update BEFORE UPDATE ON sys_dict_data
FOR EACH ROW
BEGIN
    IF NOT (NEW.deleted IN (0, NEW.id)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '字典数据删除标识只能为0或本行主键ID';
    END IF;
    IF NEW.list_class NOT IN ('default', 'primary', 'info', 'success', 'warning', 'error') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '字典数据回显样式无效';
    END IF;
END$$

DELIMITER ;

START TRANSACTION;

-- 初始管理员直接使用账号身份授权，无需创建角色或授权关系。
INSERT INTO sys_dept (id, parent_id, ancestors, dept_name, dept_code, sort, status, create_by)
VALUES (1, 0, '0', '总部', 'DEPT_1', 0, 0, 1);

INSERT INTO sys_user (id, username, super_administrator, password, name, dept_id, status, create_by)
VALUES (1, 'admin', 1, '$2b$10$COsGn7.KRbam1Wqquh3UMeBBT2XqzaQq64ozKGHeAqqchAhnWf6C.', '超级管理员', 1, 0, 1);

-- 网站文本留空时沿用部署配置，登录验证码默认开启。
INSERT INTO sys_config
    (id, config_name, config_key, value_type, value_options, config_value, remark, built_in, create_by)
VALUES
    (1, '登录验证码', 'system.captcha.enabled', 'RADIO',
     JSON_ARRAY(JSON_OBJECT('label', '开启', 'value', 'true'), JSON_OBJECT('label', '关闭', 'value', 'false')),
     'true', '登录验证码开关', 1, 1),
    (2, '网站标题', 'system.site.title', 'TEXT', NULL, NULL, '登录页、导航及浏览器标签页使用的标题，留空沿用部署配置', 1, NULL),
    (3, '网站介绍', 'system.site.description', 'TEXT', NULL, NULL, '登录页和首页的网站介绍，留空沿用部署配置', 1, NULL),
    (4, '版权信息', 'system.site.copyright', 'TEXT', NULL, NULL, '登录页与后台页脚的版权信息，留空沿用部署配置', 1, NULL);

-- 先建立导航树，再在对应页面下注册可以单独授予的操作。
INSERT INTO sys_permission
    (id, parent_id, route_name, menu_name, perms, menu_type, path, component,
     is_cache, visible, sort, icon, built_in, remark, create_by)
VALUES
    (90, 0, 'home', '首页', 'system:home:view', 2, '/index', 'layout/default/index', 1, 0, 0, 'house', 1, '默认首页组件', 1),
    (100, 0, 'System', '系统管理', NULL, 1, '/system', NULL, 0, 0, 0, 'settings', 1, NULL, 1),
    (200, 100, 'LogManagement', '日志管理', NULL, 1, '/logs', NULL, 0, 0, 20, 'scroll-text', 1, NULL, NULL),
    (1000, 100, 'User', '用户管理', 'system:user:list', 2, '/system/user', 'system/user/index', 0, 0, 0, 'users', 1, NULL, 1),
    (1100, 100, 'Role', '角色管理', 'system:role:list', 2, '/system/role', 'system/role/index', 0, 0, 0, 'shield-check', 1, NULL, 1),
    (1200, 100, 'Dept', '部门管理', 'system:dept:list', 2, '/system/dept', 'system/dept/index', 0, 0, 0, 'network', 1, NULL, 1),
    (1300, 100, 'Post', '岗位管理', 'system:post:list', 2, '/system/post', 'system/post/index', 0, 0, 0, 'briefcase-business', 1, NULL, 1),
    (1500, 100, 'SysMenu', '菜单管理', 'system:menu:list', 2, '/system/menu', 'system/menu/index', 0, 0, 50, 'menu', 1, NULL, NULL),
    (1600, 100, 'SysDictType', '字典管理', 'system:dictType:page', 2, '/system/dictType', 'system/dictType/index', 0, 0, 60, 'book-open', 1, NULL, 1),
    (1700, 100, 'SysDictData', '字典数据', 'system:dictData:page', 2, '/system/dictData/:type', 'system/dictData/index', 0, 1, 61, 'database', 1, NULL, 1),
    (1800, 100, 'SysFile', '文件管理', 'system:file:page', 2, '/system/file', 'system/file/index', 0, 0, 70, 'folder-open', 1, NULL, 1),
    (1900, 100, 'SysConfig', '系统配置', 'system:config:page', 2, '/system/config', 'system/config/index', 0, 0, 80, 'sliders-horizontal', 1, NULL, NULL),
    (3100, 200, 'SysLoginLog', '登录日志', 'system:loginLog:page', 2, '/system/loginLog', 'system/loginLog/index', 0, 0, 0, 'log-in', 1, NULL, NULL),
    (3200, 200, 'SysOperLog', '操作日志', 'system:operLog:page', 2, '/system/operLog', 'system/operLog/index', 0, 0, 1, 'list', 1, NULL, NULL);

INSERT INTO sys_permission
    (id, parent_id, menu_name, perms, menu_type, sort, built_in, create_by)
VALUES
    (1001, 1000, '用户详情', 'system:user:detail', 3, 1, 1, 1),
    (1002, 1000, '创建用户', 'system:user:create', 3, 2, 1, 1),
    (1003, 1000, '修改用户', 'system:user:update', 3, 3, 1, 1),
    (1006, 1000, '重置密码', 'system:user:resetPassword', 3, 6, 1, 1),
    (1007, 1000, '删除用户', 'system:user:delete', 3, 7, 1, 1),
    (1008, 1000, '用户角色查询', 'system:userRole:list', 3, 8, 1, 1),
    (1009, 1000, '用户角色分配', 'system:userRole:assign', 3, 9, 1, 1),
    (1101, 1100, '创建角色', 'system:role:create', 3, 1, 1, 1),
    (1102, 1100, '修改角色', 'system:role:update', 3, 2, 1, 1),
    (1103, 1100, '角色状态', 'system:role:status', 3, 3, 1, 1),
    (1104, 1100, '删除角色', 'system:role:delete', 3, 4, 1, 1),
    (1105, 1100, '角色数据范围查询', 'system:role:dataScope:list', 3, 5, 1, 1),
    (1106, 1100, '角色数据范围配置', 'system:role:dataScope:assign', 3, 6, 1, 1),
    (1107, 1100, '角色权限查询', 'system:rolePermission:list', 3, 7, 1, 1),
    (1108, 1100, '角色权限分配', 'system:rolePermission:assign', 3, 8, 1, 1),
    (1109, 1100, '角色详情', 'system:role:detail', 3, 9, 1, 1),
    (1110, 1100, '角色用户查询', 'system:roleUser:list', 3, 10, 1, NULL),
    (1111, 1100, '角色用户分配', 'system:roleUser:assign', 3, 11, 1, NULL),
    (1201, 1200, '创建部门', 'system:dept:create', 3, 1, 1, 1),
    (1202, 1200, '修改部门', 'system:dept:update', 3, 2, 1, 1),
    (1204, 1200, '删除部门', 'system:dept:delete', 3, 4, 1, 1),
    (1301, 1300, '岗位详情', 'system:post:detail', 3, 1, 1, 1),
    (1302, 1300, '创建岗位', 'system:post:create', 3, 2, 1, 1),
    (1303, 1300, '修改岗位', 'system:post:update', 3, 3, 1, 1),
    (1305, 1300, '删除岗位', 'system:post:delete', 3, 5, 1, 1),
    (1501, 1500, '菜单详情', 'system:menu:detail', 3, 1, 1, NULL),
    (1502, 1500, '新增菜单', 'system:menu:create', 3, 2, 1, NULL),
    (1503, 1500, '修改菜单', 'system:menu:update', 3, 3, 1, NULL),
    (1504, 1500, '删除菜单', 'system:menu:delete', 3, 4, 1, NULL),
    (1601, 1600, '字典详情', 'system:dictType:detail', 3, 1, 1, 1),
    (1602, 1600, '新增字典', 'system:dictType:add', 3, 2, 1, 1),
    (1603, 1600, '修改字典', 'system:dictType:update', 3, 3, 1, 1),
    (1604, 1600, '删除字典', 'system:dictType:delete', 3, 4, 1, 1),
    (1605, 1600, '导出字典', 'system:dictType:export', 3, 5, 1, 1),
    (1606, 1600, '重置字典缓存', 'system:dictType:reset', 3, 6, 1, 1),
    (1701, 1700, '字典数据详情', 'system:dictData:detail', 3, 1, 1, 1),
    (1702, 1700, '新增字典数据', 'system:dictData:add', 3, 2, 1, 1),
    (1703, 1700, '修改字典数据', 'system:dictData:update', 3, 3, 1, 1),
    (1704, 1700, '删除字典数据', 'system:dictData:delete', 3, 4, 1, 1),
    (1705, 1700, '导出字典数据', 'system:dictData:export', 3, 5, 1, 1),
    (1801, 1800, '上传文件', 'system:file:upload', 3, 1, 1, 1),
    (1802, 1800, '下载文件', 'system:file:download', 3, 2, 1, 1),
    (1803, 1800, '删除文件', 'system:file:delete', 3, 3, 1, 1),
    (1804, 1800, '存储配置查询', 'system:file:storageList', 3, 4, 1, 1),
    (1805, 1800, '新增存储配置', 'system:file:addStorage', 3, 5, 1, 1),
    (1806, 1800, '修改存储配置', 'system:file:updateStorage', 3, 6, 1, 1),
    (1807, 1800, '切换默认存储', 'system:file:defaultStorage', 3, 7, 1, 1),
    (1808, 1800, '删除存储配置', 'system:file:deleteStorage', 3, 8, 1, 1),
    (1901, 1900, '新增自定义配置', 'system:config:create', 3, 1, 1, NULL),
    (1902, 1900, '修改配置', 'system:config:update', 3, 2, 1, NULL),
    (1903, 1900, '删除自定义配置', 'system:config:delete', 3, 3, 1, NULL),
    (3101, 3100, '导出登录日志', 'system:loginLog:export', 3, 1, 1, NULL),
    (3102, 3100, '删除登录日志', 'system:loginLog:delete', 3, 2, 1, NULL),
    (3103, 3100, '清空登录日志', 'system:loginLog:clean', 3, 3, 1, NULL),
    (3201, 3200, '操作日志详情', 'system:operLog:detail', 3, 1, 1, NULL),
    (3202, 3200, '导出操作日志', 'system:operLog:export', 3, 2, 1, NULL),
    (3203, 3200, '删除操作日志', 'system:operLog:delete', 3, 3, 1, NULL),
    (3204, 3200, '清空操作日志', 'system:operLog:clean', 3, 4, 1, NULL);

INSERT INTO sys_dict_type
    (id, name, type, remark, create_by)
VALUES
    (2000, '菜单类型', 'sys_menu_type', '对应菜单类型编码：1目录，2菜单，3按钮，4外链', 1),
    (2001, '通用状态', 'sys_common_status', '通用启停状态：0启用，1停用，供菜单、部门、岗位、用户等业务共用', 1),
    (2002, '显示状态', 'sys_visible_status', '通用显示状态：0显示，1隐藏，供可见性配置共用', 1),
    (2003, '菜单缓存状态', 'sys_menu_cache', '对应页面缓存配置：0不缓存，1缓存', 1),
    (2004, '用户性别', 'sys_user_gender', '用户性别：0未知，1男，2女', 1),
    (2005, '文件存储类型', 'sys_file_storage_platform', '文件模块支持的存储类型', 1),
    (2006, '文件上传途径', 'sys_file_upload_source', '文件所属业务上传入口，与存储平台和文件类型无关', 1),
    (2007, '角色数据范围', 'sys_role_data_scope', '每个角色的全部操作共用同一个数据范围', 1),
    (2008, '执行结果', 'sys_result_status', '0成功，1失败', 1),
    (2009, '操作类型', 'sys_oper_type', '系统操作日志使用的业务操作类型', 1);

INSERT INTO sys_dict_data
    (id, type, label, value, list_class, sort, create_by)
VALUES
    (20000, 'sys_menu_type', '目录', '1', 'info', 0, 1),
    (20001, 'sys_menu_type', '菜单', '2', 'success', 1, 1),
    (20002, 'sys_menu_type', '按钮', '3', 'warning', 2, 1),
    (20003, 'sys_menu_type', '外链', '4', 'primary', 3, 1),
    (20010, 'sys_common_status', '启用', '0', 'success', 0, 1),
    (20011, 'sys_common_status', '停用', '1', 'error', 1, 1),
    (20020, 'sys_visible_status', '显示', '0', 'success', 0, 1),
    (20021, 'sys_visible_status', '隐藏', '1', 'warning', 1, 1),
    (20030, 'sys_menu_cache', '不缓存', '0', 'default', 0, 1),
    (20031, 'sys_menu_cache', '缓存', '1', 'success', 1, 1),
    (20040, 'sys_user_gender', '未知', '0', 'default', 0, 1),
    (20041, 'sys_user_gender', '男', '1', 'info', 1, 1),
    (20042, 'sys_user_gender', '女', '2', 'warning', 2, 1),
    (20050, 'sys_file_storage_platform', '本地存储', 'local', 'default', 0, 1),
    (20051, 'sys_file_storage_platform', 'MinIO', 'minio', 'info', 1, 1),
    (20052, 'sys_file_storage_platform', '阿里云 OSS', 'aliyunOss', 'warning', 2, 1),
    (20060, 'sys_file_upload_source', '文件管理', 'fileManagement', 'default', 0, 1),
    (20061, 'sys_file_upload_source', '用户头像', 'userAvatar', 'default', 1, 1),
    (20070, 'sys_role_data_scope', '全部数据', '1', 'primary', 0, 1),
    (20071, 'sys_role_data_scope', '自定义部门', '2', 'info', 1, 1),
    (20072, 'sys_role_data_scope', '本部门', '3', 'default', 2, 1),
    (20073, 'sys_role_data_scope', '本部门及下级部门', '4', 'default', 3, 1),
    (20074, 'sys_role_data_scope', '仅本人', '5', 'default', 4, 1),
    (20080, 'sys_result_status', '成功', '0', 'success', 0, 1),
    (20081, 'sys_result_status', '失败', '1', 'error', 1, 1),
    (20090, 'sys_oper_type', '新增', '0', 'success', 0, 1),
    (20091, 'sys_oper_type', '修改', '1', 'info', 1, 1),
    (20092, 'sys_oper_type', '删除', '2', 'error', 2, 1),
    (20093, 'sys_oper_type', '导出', '3', 'primary', 3, 1),
    (20094, 'sys_oper_type', '导入', '4', 'primary', 4, 1),
    (20095, 'sys_oper_type', '其他', '5', 'default', 5, 1),
    (20096, 'sys_oper_type', '清空', '6', 'warning', 6, 1);

COMMIT;
