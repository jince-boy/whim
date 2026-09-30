-- Whim 单组织 RBAC 初始化；面向空库，MySQL 8.0.16+。
-- 主键由 MyBatis-Plus ASSIGN_ID 生成；关联关系物理删除，业务实体逻辑删除。
SET NAMES utf8mb4;

CREATE TABLE sys_dept (
    id BIGINT NOT NULL COMMENT '主键ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父部门ID，0表示根节点',
    ancestors VARCHAR(500) NOT NULL DEFAULT '0' COMMENT '祖先路径',
    dept_name VARCHAR(64) NOT NULL,
    leader_user_id BIGINT NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(64) NULL,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0正常，1停用',
    remark VARCHAR(255) NULL,
    active_name VARCHAR(64) GENERATED ALWAYS AS (IF(deleted = 0, dept_name, NULL)) STORED,
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0正常，1删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dept_parent_name (parent_id, active_name),
    KEY idx_dept_parent (parent_id, deleted, status),
    CONSTRAINT ck_dept_status CHECK (status IN (0, 1)),
    CONSTRAINT ck_sys_dept_deleted CHECK (deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门';

CREATE TABLE sys_user (
    id BIGINT NOT NULL COMMENT '主键ID',
    username VARCHAR(32) NOT NULL COMMENT '规范化为小写，删除后仍保留账号唯一性',
    password VARCHAR(128) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    avatar VARCHAR(255) NULL,
    name VARCHAR(24) NOT NULL,
    email VARCHAR(64) NULL,
    phone VARCHAR(20) NULL,
    gender TINYINT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0正常，1停用',
    dept_id BIGINT NOT NULL COMMENT '唯一主部门',
    remark VARCHAR(255) NULL,
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0正常，1删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username (username),
    KEY idx_user_dept (dept_id, deleted, status),
    CONSTRAINT fk_user_dept FOREIGN KEY (dept_id) REFERENCES sys_dept (id),
    CONSTRAINT ck_user_status CHECK (status IN (0, 1)),
    CONSTRAINT ck_user_gender CHECK (gender BETWEEN 0 AND 2),
    CONSTRAINT ck_sys_user_deleted CHECK (deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='全局唯一用户';

CREATE TABLE sys_role (
    id BIGINT NOT NULL COMMENT '主键ID',
    role_name VARCHAR(32) NOT NULL,
    role_code VARCHAR(128) NOT NULL,
    data_scope TINYINT NOT NULL DEFAULT 5 COMMENT '1全部，2自定义部门，3本部门，4本部门及下级，5本人',
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0正常，1停用',
    remark VARCHAR(255) NULL,
    active_code VARCHAR(128) GENERATED ALWAYS AS (IF(deleted = 0, role_code, NULL)) STORED,
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0正常，1删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (active_code),
    CONSTRAINT ck_role_status CHECK (status IN (0, 1)),
    CONSTRAINT ck_role_scope CHECK (data_scope BETWEEN 1 AND 5),
    CONSTRAINT ck_sys_role_deleted CHECK (deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色';

CREATE TABLE sys_permission (
    id BIGINT NOT NULL COMMENT '主键ID',
    route_name VARCHAR(128) NULL,
    menu_name VARCHAR(64) NOT NULL,
    perms VARCHAR(128) NULL COMMENT '精确操作权限码，目录不设置权限码',
    data_permission TINYINT NOT NULL DEFAULT 0 COMMENT '0功能权限，1同时使用数据范围',
    parent_id BIGINT NOT NULL DEFAULT 0,
    menu_type TINYINT NOT NULL COMMENT '1目录，2菜单，3按钮，4外链',
    path VARCHAR(255) NULL,
    param VARCHAR(255) NULL,
    component VARCHAR(255) NULL,
    is_cache TINYINT NOT NULL DEFAULT 0,
    sort INT NOT NULL DEFAULT 0,
    icon VARCHAR(128) NULL,
    visible TINYINT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0正常，1停用',
    remark VARCHAR(255) NULL,
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0正常，1删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_perms (perms),
    KEY idx_permission_parent (parent_id, deleted, status),
    CONSTRAINT ck_permission_status CHECK (status IN (0, 1)),
    CONSTRAINT ck_permission_data CHECK (data_permission IN (0, 1)),
    CONSTRAINT ck_permission_type CHECK (menu_type BETWEEN 1 AND 4),
    CONSTRAINT ck_permission_definition CHECK (
        (menu_type IN (1, 4) AND perms IS NULL AND data_permission = 0)
        OR (menu_type IN (2, 3) AND perms IS NOT NULL AND CHAR_LENGTH(TRIM(perms)) > 0)
    ),
    CONSTRAINT ck_sys_permission_deleted CHECK (deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='功能权限和菜单定义';

CREATE TABLE sys_post (
    id BIGINT NOT NULL COMMENT '主键ID',
    dept_id BIGINT NULL COMMENT '所属部门，空表示全组织岗位',
    owner_user_id BIGINT NOT NULL COMMENT '业务负责人，独立于创建审计',
    post_name VARCHAR(64) NOT NULL,
    post_code VARCHAR(64) NOT NULL,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0正常，1停用',
    remark VARCHAR(255) NULL,
    active_code VARCHAR(64) GENERATED ALWAYS AS (IF(deleted = 0, post_code, NULL)) STORED,
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0正常，1删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_code (active_code),
    KEY idx_post_dept (dept_id, deleted, status),
    KEY idx_post_owner (owner_user_id, deleted),
    CONSTRAINT fk_post_dept FOREIGN KEY (dept_id) REFERENCES sys_dept (id),
    CONSTRAINT fk_post_owner FOREIGN KEY (owner_user_id) REFERENCES sys_user (id),
    CONSTRAINT ck_post_status CHECK (status IN (0, 1)),
    CONSTRAINT ck_sys_post_deleted CHECK (deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='岗位，独立于角色授权';

CREATE TABLE sys_config (
    id BIGINT NOT NULL COMMENT '主键ID',
    config_key VARCHAR(128) NOT NULL,
    config_value LONGTEXT NULL,
    remark VARCHAR(255) NULL,
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '删除标志：0正常，1删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key),
    CONSTRAINT ck_sys_config_deleted CHECK (deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置';

CREATE TABLE sys_user_role (
    id BIGINT NOT NULL COMMENT '主键ID',
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联，物理删除';

CREATE TABLE sys_user_post (
    id BIGINT NOT NULL COMMENT '主键ID',
    user_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户岗位关联，物理删除';

CREATE TABLE sys_role_permission (
    id BIGINT NOT NULL COMMENT '主键ID',
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    data_scope TINYINT NULL COMMENT '空继承角色默认值，1至5表示本操作覆盖',
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
    CONSTRAINT fk_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission (id),
    CONSTRAINT ck_role_permission_scope CHECK (data_scope IS NULL OR data_scope BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色操作授权和覆盖范围';

CREATE TABLE sys_role_dept (
    id BIGINT NOT NULL COMMENT '主键ID',
    role_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色默认自定义部门范围';

CREATE TABLE sys_role_permission_dept (
    id BIGINT NOT NULL COMMENT '主键ID',
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    create_by BIGINT NULL COMMENT '创建人ID',
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    update_by BIGINT NULL COMMENT '更新人ID',
    update_time DATETIME(3) NULL COMMENT '更新时间',
    delete_by BIGINT NULL COMMENT '删除人ID',
    delete_time DATETIME(3) NULL COMMENT '删除时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission_dept (role_id, permission_id, dept_id),
    KEY idx_role_permission_dept_dept (dept_id),
    CONSTRAINT fk_role_permission_dept_binding FOREIGN KEY (role_id, permission_id) REFERENCES sys_role_permission (role_id, permission_id),
    CONSTRAINT fk_role_permission_dept_dept FOREIGN KEY (dept_id) REFERENCES sys_dept (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作覆盖自定义部门范围';

INSERT INTO sys_dept (id, parent_id, ancestors, dept_name, sort, status, create_by)
VALUES (1, 0, '0', '总部', 0, 0, 1);

INSERT INTO sys_user (id, username, password, name, dept_id, status, create_by)
VALUES (1, 'admin', '$2b$10$COsGn7.KRbam1Wqquh3UMeBBT2XqzaQq64ozKGHeAqqchAhnWf6C.', '超级管理员', 1, 0, 1);

INSERT INTO sys_role (id, role_name, role_code, data_scope, status, create_by)
VALUES (1, '超级管理员', 'superadmin', 1, 0, 1);

INSERT INTO sys_user_role (id, user_id, role_id, create_by)
VALUES (1, 1, 1, 1);

INSERT INTO sys_config (id, config_key, config_value, remark, create_by)
VALUES (1, 'system.captcha.enabled', 'true', '登录验证码开关', 1);

-- 目录只参与导航；菜单和按钮权限必须分别授予。
INSERT INTO sys_permission
    (id, route_name, menu_name, perms, data_permission, parent_id, menu_type, path, component, sort, create_by)
VALUES
    (100, 'System', '系统管理', NULL, 0, 0, 1, '/system', 'Layout', 0, 1),
    (1000, 'User', '用户列表', 'system:user:list', 1, 100, 2, '/system/user', 'system/user/index', 0, 1),
    (1001, NULL, '用户详情', 'system:user:detail', 1, 1000, 3, NULL, NULL, 1, 1),
    (1002, NULL, '创建用户', 'system:user:create', 1, 1000, 3, NULL, NULL, 2, 1),
    (1003, NULL, '修改用户', 'system:user:update', 1, 1000, 3, NULL, NULL, 3, 1),
    (1004, NULL, '调整用户部门', 'system:user:department', 1, 1000, 3, NULL, NULL, 4, 1),
    (1005, NULL, '用户状态', 'system:user:status', 1, 1000, 3, NULL, NULL, 5, 1),
    (1006, NULL, '重置密码', 'system:user:resetPassword', 1, 1000, 3, NULL, NULL, 6, 1),
    (1007, NULL, '删除用户', 'system:user:delete', 1, 1000, 3, NULL, NULL, 7, 1),
    (1008, NULL, '用户角色查询', 'system:userRole:list', 1, 1000, 3, NULL, NULL, 8, 1),
    (1009, NULL, '用户角色分配', 'system:userRole:assign', 1, 1000, 3, NULL, NULL, 9, 1),
    (1100, 'Role', '角色列表', 'system:role:list', 0, 100, 2, '/system/role', 'system/role/index', 0, 1),
    (1101, NULL, '创建角色', 'system:role:create', 0, 1100, 3, NULL, NULL, 1, 1),
    (1102, NULL, '修改角色', 'system:role:update', 0, 1100, 3, NULL, NULL, 2, 1),
    (1103, NULL, '角色状态', 'system:role:status', 0, 1100, 3, NULL, NULL, 3, 1),
    (1104, NULL, '删除角色', 'system:role:delete', 0, 1100, 3, NULL, NULL, 4, 1),
    (1105, NULL, '角色数据范围查询', 'system:role:dataScope:list', 0, 1100, 3, NULL, NULL, 5, 1),
    (1106, NULL, '角色数据范围配置', 'system:role:dataScope:assign', 0, 1100, 3, NULL, NULL, 6, 1),
    (1107, NULL, '角色权限查询', 'system:rolePermission:list', 0, 1100, 3, NULL, NULL, 7, 1),
    (1108, NULL, '角色权限分配', 'system:rolePermission:assign', 0, 1100, 3, NULL, NULL, 8, 1),
    (1200, 'Dept', '部门列表', 'system:dept:list', 1, 100, 2, '/system/dept', 'system/dept/index', 0, 1),
    (1201, NULL, '创建部门', 'system:dept:create', 1, 1200, 3, NULL, NULL, 1, 1),
    (1202, NULL, '修改部门', 'system:dept:update', 1, 1200, 3, NULL, NULL, 2, 1),
    (1203, NULL, '部门状态', 'system:dept:status', 1, 1200, 3, NULL, NULL, 3, 1),
    (1204, NULL, '删除部门', 'system:dept:delete', 1, 1200, 3, NULL, NULL, 4, 1),
    (1300, 'Post', '岗位列表', 'system:post:list', 1, 100, 2, '/system/post', 'system/post/index', 0, 1),
    (1301, NULL, '岗位详情', 'system:post:detail', 1, 1300, 3, NULL, NULL, 1, 1),
    (1302, NULL, '创建岗位', 'system:post:create', 1, 1300, 3, NULL, NULL, 2, 1),
    (1303, NULL, '修改岗位', 'system:post:update', 1, 1300, 3, NULL, NULL, 3, 1),
    (1304, NULL, '岗位状态', 'system:post:status', 1, 1300, 3, NULL, NULL, 4, 1),
    (1305, NULL, '删除岗位', 'system:post:delete', 1, 1300, 3, NULL, NULL, 5, 1),
    (1306, NULL, '岗位成员查询', 'system:post:member:list', 1, 1300, 3, NULL, NULL, 6, 1),
    (1307, NULL, '岗位成员配置', 'system:post:member:assign', 1, 1300, 3, NULL, NULL, 7, 1),
    (1400, 'Permission', '权限目录', 'system:permission:list', 0, 100, 2, '/system/permission', 'system/permission/index', 0, 1),
    (1401, NULL, '权限状态', 'system:permission:status', 0, 1400, 3, NULL, NULL, 1, 1);

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
    (35, 1, 1401, 1);
