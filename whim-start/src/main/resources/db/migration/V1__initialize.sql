-- Whim 数据库初始化；面向空库，MySQL 8.0.12+。
-- 主键由 MyBatis-Plus ASSIGN_ID 生成；关联关系物理删除，业务实体逻辑删除。
-- 业务键与 deleted 组成联合唯一索引；MySQL 8.0.12 不执行 CHECK，字段取值由触发器校验。
-- 字典类型和数据项不设状态字段，数据项通过 type 编码关联类型。
SET NAMES utf8mb4;

CREATE TABLE sys_dept (
    id BIGINT NOT NULL COMMENT '主键ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父部门ID，0表示根节点',
    ancestors VARCHAR(500) NOT NULL DEFAULT '0' COMMENT '祖级部门ID路径，逗号分隔',
    dept_name VARCHAR(64) NOT NULL COMMENT '部门名称',
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
    KEY idx_dept_parent (parent_id, deleted, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';

CREATE TABLE sys_user (
    id BIGINT NOT NULL COMMENT '主键ID',
    username VARCHAR(32) NOT NULL COMMENT '用户名，统一小写且删除后不可重复使用',
    password VARCHAR(128) CHARACTER SET ascii COLLATE ascii_bin NOT NULL COMMENT '密码哈希值',
    avatar VARCHAR(255) NULL COMMENT '头像URL',
    name VARCHAR(24) NOT NULL COMMENT '真实姓名',
    email VARCHAR(64) NULL COMMENT '邮箱',
    phone VARCHAR(20) NULL COMMENT '手机号码',
    gender TINYINT NOT NULL DEFAULT 0 COMMENT '性别字典编码',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0正常，1停用',
    dept_id BIGINT NOT NULL COMMENT '主部门ID',
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
    perms VARCHAR(128) NULL COMMENT '操作权限码，目录和外链不设置',
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
    owner_user_id BIGINT NOT NULL COMMENT '业务负责人用户ID',
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
    KEY idx_post_owner (owner_user_id, deleted),
    CONSTRAINT fk_post_dept FOREIGN KEY (dept_id) REFERENCES sys_dept (id),
    CONSTRAINT fk_post_owner FOREIGN KEY (owner_user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位表';

CREATE TABLE sys_config (
    id BIGINT NOT NULL COMMENT '主键ID',
    config_key VARCHAR(128) NOT NULL COMMENT '配置键',
    config_value LONGTEXT NULL COMMENT '配置值',
    remark VARCHAR(255) NULL COMMENT '备注',
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    deleted BIGINT NOT NULL DEFAULT 0 COMMENT '删除标识：0未删除，已删除时为本行主键ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
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

-- 插入与更新均校验原 CHECK 规则，避免 MySQL 8.0.12 忽略约束后写入非法数据。
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
    IF NOT ((NEW.menu_type IN (1, 4) AND NEW.perms IS NULL)
            OR (NEW.menu_type IN (2, 3) AND NEW.perms IS NOT NULL AND CHAR_LENGTH(TRIM(NEW.perms)) > 0)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '目录和外链不能设置权限码，菜单和按钮必须设置非空权限码';
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
    IF NOT ((NEW.menu_type IN (1, 4) AND NEW.perms IS NULL)
            OR (NEW.menu_type IN (2, 3) AND NEW.perms IS NOT NULL AND CHAR_LENGTH(TRIM(NEW.perms)) > 0)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '目录和外链不能设置权限码，菜单和按钮必须设置非空权限码';
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

INSERT INTO sys_dept (id, parent_id, ancestors, dept_name, sort, status, create_by)
VALUES (1, 0, '0', '总部', 0, 0, 1);

INSERT INTO sys_user (id, username, password, name, dept_id, status, create_by)
VALUES (1, 'admin', '$2b$10$COsGn7.KRbam1Wqquh3UMeBBT2XqzaQq64ozKGHeAqqchAhnWf6C.', '超级管理员', 1, 0, 1);

INSERT INTO sys_role (id, role_name, role_code, data_scope, status, create_by)
VALUES (1, '超级管理员', 'superadmin', 1, 0, 1);

INSERT INTO sys_user_role (id, user_id, role_id, create_by)
VALUES (1, 1, 1, 1);

INSERT INTO sys_config (id, config_key, config_value, remark, create_by)
VALUES
    (1, 'system.captcha.enabled', 'true', '登录验证码开关', 1),
    (2, 'system.menu.lock', '1', '菜单目录并发维护锁', NULL);

-- 首页与其他页面共用菜单授权，首页分配由独立配置维护。
INSERT INTO sys_permission
    (id, route_name, menu_name, perms, parent_id, menu_type, path, component,
     is_cache, sort, icon, built_in, remark, create_by)
VALUES
    (90, 'home', '首页', 'system:home:view', 0, 2, '/index', 'layout/default/index',
     1, 0, 'smile', 1, '默认首页组件，首页分配由独立配置维护', 1);

-- 目录只参与导航；菜单和按钮权限必须分别授予。
INSERT INTO sys_permission
    (id, route_name, menu_name, perms, parent_id, menu_type, path, component, sort, built_in, create_by)
VALUES
    (100, 'System', '系统管理', NULL, 0, 1, '/system', NULL, 0, 1, 1),
    (1000, 'User', '用户列表', 'system:user:list', 100, 2, '/system/user', 'system/user/index', 0, 1, 1),
    (1001, NULL, '用户详情', 'system:user:detail', 1000, 3, NULL, NULL, 1, 1, 1),
    (1002, NULL, '创建用户', 'system:user:create', 1000, 3, NULL, NULL, 2, 1, 1),
    (1003, NULL, '修改用户', 'system:user:update', 1000, 3, NULL, NULL, 3, 1, 1),
    (1004, NULL, '调整用户部门', 'system:user:department', 1000, 3, NULL, NULL, 4, 1, 1),
    (1005, NULL, '用户状态', 'system:user:status', 1000, 3, NULL, NULL, 5, 1, 1),
    (1006, NULL, '重置密码', 'system:user:resetPassword', 1000, 3, NULL, NULL, 6, 1, 1),
    (1007, NULL, '删除用户', 'system:user:delete', 1000, 3, NULL, NULL, 7, 1, 1),
    (1008, NULL, '用户角色查询', 'system:userRole:list', 1000, 3, NULL, NULL, 8, 1, 1),
    (1009, NULL, '用户角色分配', 'system:userRole:assign', 1000, 3, NULL, NULL, 9, 1, 1),
    (1100, 'Role', '角色列表', 'system:role:list', 100, 2, '/system/role', 'system/role/index', 0, 1, 1),
    (1101, NULL, '创建角色', 'system:role:create', 1100, 3, NULL, NULL, 1, 1, 1),
    (1102, NULL, '修改角色', 'system:role:update', 1100, 3, NULL, NULL, 2, 1, 1),
    (1103, NULL, '角色状态', 'system:role:status', 1100, 3, NULL, NULL, 3, 1, 1),
    (1104, NULL, '删除角色', 'system:role:delete', 1100, 3, NULL, NULL, 4, 1, 1),
    (1105, NULL, '角色数据范围查询', 'system:role:dataScope:list', 1100, 3, NULL, NULL, 5, 1, 1),
    (1106, NULL, '角色数据范围配置', 'system:role:dataScope:assign', 1100, 3, NULL, NULL, 6, 1, 1),
    (1107, NULL, '角色权限查询', 'system:rolePermission:list', 1100, 3, NULL, NULL, 7, 1, 1),
    (1108, NULL, '角色权限分配', 'system:rolePermission:assign', 1100, 3, NULL, NULL, 8, 1, 1),
    (1200, 'Dept', '部门列表', 'system:dept:list', 100, 2, '/system/dept', 'system/dept/index', 0, 1, 1),
    (1201, NULL, '创建部门', 'system:dept:create', 1200, 3, NULL, NULL, 1, 1, 1),
    (1202, NULL, '修改部门', 'system:dept:update', 1200, 3, NULL, NULL, 2, 1, 1),
    (1203, NULL, '部门状态', 'system:dept:status', 1200, 3, NULL, NULL, 3, 1, 1),
    (1204, NULL, '删除部门', 'system:dept:delete', 1200, 3, NULL, NULL, 4, 1, 1),
    (1300, 'Post', '岗位列表', 'system:post:list', 100, 2, '/system/post', 'system/post/index', 0, 1, 1),
    (1301, NULL, '岗位详情', 'system:post:detail', 1300, 3, NULL, NULL, 1, 1, 1),
    (1302, NULL, '创建岗位', 'system:post:create', 1300, 3, NULL, NULL, 2, 1, 1),
    (1303, NULL, '修改岗位', 'system:post:update', 1300, 3, NULL, NULL, 3, 1, 1),
    (1304, NULL, '岗位状态', 'system:post:status', 1300, 3, NULL, NULL, 4, 1, 1),
    (1305, NULL, '删除岗位', 'system:post:delete', 1300, 3, NULL, NULL, 5, 1, 1),
    (1306, NULL, '岗位成员查询', 'system:post:member:list', 1300, 3, NULL, NULL, 6, 1, 1),
    (1307, NULL, '岗位成员配置', 'system:post:member:assign', 1300, 3, NULL, NULL, 7, 1, 1),
    (1400, NULL, '权限目录', 'system:permission:list', 1500, 3, NULL, NULL, 0, 1, 1),
    (1401, NULL, '权限状态', 'system:permission:status', 1500, 3, NULL, NULL, 1, 1, 1);

-- 菜单管理统一维护菜单与权限，权限目录和权限状态作为按钮权限保留。
INSERT INTO sys_permission
    (id, route_name, menu_name, perms, parent_id, menu_type, path, component, sort, icon, built_in)
VALUES
    (1500, 'SysMenu', '菜单管理', 'system:menu:list', 100, 2, '/system/menu', 'system/menu/index', 50, 'yonghu', 1),
    (1501, NULL, '菜单详情', 'system:menu:detail', 1500, 3, NULL, NULL, 1, NULL, 1),
    (1502, NULL, '新增菜单', 'system:menu:create', 1500, 3, NULL, NULL, 2, NULL, 1),
    (1503, NULL, '修改菜单', 'system:menu:update', 1500, 3, NULL, NULL, 3, NULL, 1),
    (1504, NULL, '删除菜单', 'system:menu:delete', 1500, 3, NULL, NULL, 4, NULL, 1);

-- 内置角色不可编辑；同时保留完整授权关系，方便管理端展示。
INSERT INTO sys_role_permission (id, role_id, permission_id, create_by)
VALUES
    (1, 1, 100, 1),
    (2, 1, 1000, 1),
    (3, 1, 1001, 1),
    (4, 1, 1002, 1),
    (5, 1, 1003, 1),
    (6, 1, 1004, 1),
    (7, 1, 1005, 1),
    (8, 1, 1006, 1),
    (9, 1, 1007, 1),
    (10, 1, 1008, 1),
    (11, 1, 1009, 1),
    (12, 1, 1100, 1),
    (13, 1, 1101, 1),
    (14, 1, 1102, 1),
    (15, 1, 1103, 1),
    (16, 1, 1104, 1),
    (17, 1, 1105, 1),
    (18, 1, 1106, 1),
    (19, 1, 1107, 1),
    (20, 1, 1108, 1),
    (21, 1, 1200, 1),
    (22, 1, 1201, 1),
    (23, 1, 1202, 1),
    (24, 1, 1203, 1),
    (25, 1, 1204, 1),
    (26, 1, 1300, 1),
    (27, 1, 1301, 1),
    (28, 1, 1302, 1),
    (29, 1, 1303, 1),
    (30, 1, 1304, 1),
    (31, 1, 1305, 1),
    (32, 1, 1306, 1),
    (33, 1, 1307, 1),
    (34, 1, 1400, 1),
    (35, 1, 1401, 1),
    (36, 1, 1500, NULL),
    (37, 1, 1501, NULL),
    (38, 1, 1502, NULL),
    (39, 1, 1503, NULL),
    (40, 1, 1504, NULL),
    (41, 1, 90, 1);

-- 注册字典管理菜单与接口权限；字典选项查询仅要求系统账号登录。
-- 字典为全局共享配置，不绑定部门归属或按用户的数据范围。
INSERT INTO sys_permission
    (id, route_name, menu_name, perms, parent_id, menu_type, path, component, visible, sort, built_in, create_by)
VALUES
    (1600, 'SysDictType', '字典类型', 'system:dictType:page', 100, 2, '/system/dictType', 'system/dictType/index', 0, 60, 1, 1),
    (1601, NULL, '字典类型详情', 'system:dictType:detail', 1600, 3, NULL, NULL, 0, 1, 1, 1),
    (1602, NULL, '新增字典类型', 'system:dictType:add', 1600, 3, NULL, NULL, 0, 2, 1, 1),
    (1603, NULL, '修改字典类型', 'system:dictType:update', 1600, 3, NULL, NULL, 0, 3, 1, 1),
    (1604, NULL, '删除字典类型', 'system:dictType:delete', 1600, 3, NULL, NULL, 0, 4, 1, 1),
    (1605, NULL, '导出字典类型', 'system:dictType:export', 1600, 3, NULL, NULL, 0, 5, 1, 1),
    (1606, NULL, '重置字典缓存', 'system:dictType:reset', 1600, 3, NULL, NULL, 0, 6, 1, 1),
    (1700, 'SysDictData', '字典数据', 'system:dictData:page', 100, 2, '/system/dictData/:type', 'system/dictData/index', 1, 61, 1, 1),
    (1701, NULL, '字典数据详情', 'system:dictData:detail', 1700, 3, NULL, NULL, 0, 1, 1, 1),
    (1702, NULL, '新增字典数据', 'system:dictData:add', 1700, 3, NULL, NULL, 0, 2, 1, 1),
    (1703, NULL, '修改字典数据', 'system:dictData:update', 1700, 3, NULL, NULL, 0, 3, 1, 1),
    (1704, NULL, '删除字典数据', 'system:dictData:delete', 1700, 3, NULL, NULL, 0, 4, 1, 1),
    (1705, NULL, '导出字典数据', 'system:dictData:export', 1700, 3, NULL, NULL, 0, 5, 1, 1);

-- 为内置超级管理员补齐授权关系，其他角色按需分配。
INSERT INTO sys_role_permission (id, role_id, permission_id, create_by)
SELECT 160000 + id, 1, id, 1
FROM sys_permission
WHERE id IN (1600, 1601, 1602, 1603, 1604, 1605, 1606, 1700, 1701, 1702, 1703, 1704, 1705);
