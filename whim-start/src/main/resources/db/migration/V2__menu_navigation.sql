-- 菜单导航与管理。保留已应用的 V1，不复用历史权限码。
ALTER TABLE sys_permission
    ADD COLUMN built_in TINYINT NOT NULL DEFAULT 0 COMMENT '内置定义不能删除或改变授权语义',
    ADD COLUMN active_route VARCHAR(128) GENERATED ALWAYS AS
        (IF(deleted = 0 AND menu_type IN (1, 2), route_name, NULL)) STORED,
    ADD COLUMN active_path VARCHAR(255) GENERATED ALWAYS AS
        (IF(deleted = 0 AND menu_type IN (1, 2), path, NULL)) STORED,
    ADD UNIQUE KEY uk_menu_route (active_route),
    ADD UNIQUE KEY uk_menu_path (active_path),
    ADD CONSTRAINT ck_menu_built_in CHECK (built_in IN (0, 1));

UPDATE sys_permission SET built_in = 1;
INSERT INTO sys_config (id, config_key, config_value, remark)
VALUES (2, 'system.menu.lock', '1', '菜单目录并发维护锁');

INSERT INTO sys_permission
    (id, route_name, menu_name, perms, parent_id, menu_type, path, component, sort, icon, built_in)
VALUES
    (1500, 'SysMenu', '菜单管理', 'system:menu:list', 100, 2, '/system/menu', 'system/menu/index', 50, 'menu', 1),
    (1501, NULL, '菜单详情', 'system:menu:detail', 1500, 3, NULL, NULL, 1, NULL, 1),
    (1502, NULL, '新增菜单', 'system:menu:create', 1500, 3, NULL, NULL, 2, NULL, 1),
    (1503, NULL, '修改菜单', 'system:menu:update', 1500, 3, NULL, NULL, 3, NULL, 1),
    (1504, NULL, '删除菜单', 'system:menu:delete', 1500, 3, NULL, NULL, 4, NULL, 1);

-- 权限目录继续作为独立后端操作，管理页面统一使用菜单管理。
UPDATE sys_permission SET menu_type = 3, parent_id = 1500, route_name = NULL,
    path = NULL, component = NULL WHERE id = 1400;
UPDATE sys_permission SET parent_id = 1500 WHERE id = 1401;
UPDATE sys_permission SET component = NULL, icon = 'setting' WHERE id = 100;

INSERT INTO sys_role_permission (id, role_id, permission_id)
VALUES (36, 1, 1500), (37, 1, 1501), (38, 1, 1502), (39, 1, 1503), (40, 1, 1504);
