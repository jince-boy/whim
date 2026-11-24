CREATE TABLE sys_home_page (
    id BIGINT NOT NULL,
    target_type TINYINT NOT NULL COMMENT '1用户，2角色，3部门，4全局',
    target_id BIGINT NOT NULL COMMENT '全局使用0',
    path VARCHAR(255) NOT NULL,
    component VARCHAR(255) NOT NULL,
    menu_id BIGINT NULL COMMENT '复用的菜单ID',
    priority INT NOT NULL DEFAULT 0 COMMENT '数值越小越优先',
    status TINYINT NOT NULL DEFAULT 0,
    create_by BIGINT NULL,
    create_time DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    update_by BIGINT NULL,
    update_time DATETIME(3) NULL,
    delete_by BIGINT NULL,
    delete_time DATETIME(3) NULL,
    deleted BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_home_target (target_type, target_id, deleted),
    KEY idx_home_path (path, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='首页配置';

INSERT INTO sys_home_page (id, target_type, target_id, path, component, menu_id, priority, status)
SELECT 1, 4, 0, CASE WHEN path LIKE '/%' THEN path ELSE CONCAT('/', path) END,
       CASE WHEN component IN ('layout/default/index', 'layout/default/index.vue') THEN 'home/index'
            WHEN RIGHT(component, 4) = '.vue' THEN LEFT(component, CHAR_LENGTH(component) - 4)
            ELSE component END, id, 0, 0
FROM sys_permission WHERE built_in = 1 AND perms = 'system:home:view' AND deleted = 0 AND component IS NOT NULL AND path IS NOT NULL;

INSERT INTO sys_permission
    (id, parent_id, route_name, menu_name, perms, menu_type, path, component, is_cache, visible, sort, icon, built_in)
VALUES (2300, 100, 'HomePageManagement', '首页管理', 'system:homePage:page', 2, '/system/homePage', 'system/homePage/index', 0, 0, 51, 'house', 1);
INSERT INTO sys_permission (id, parent_id, menu_name, perms, menu_type, sort, built_in) VALUES
    (2301, 2300, '新增首页配置', 'system:homePage:add', 3, 1, 1),
    (2302, 2300, '修改首页配置', 'system:homePage:update', 3, 2, 1),
    (2303, 2300, '删除首页配置', 'system:homePage:delete', 3, 3, 1);

INSERT INTO sys_dict_type (id, name, type) VALUES (2300, '首页关联类型', 'sys_home_target_type');
INSERT INTO sys_dict_data (id, type, label, value, list_class, sort) VALUES
    (23001, 'sys_home_target_type', '用户', '1', 'info', 1),
    (23002, 'sys_home_target_type', '角色', '2', 'primary', 2),
    (23003, 'sys_home_target_type', '部门', '3', 'success', 3),
    (23004, 'sys_home_target_type', '全局默认', '4', 'default', 4);
