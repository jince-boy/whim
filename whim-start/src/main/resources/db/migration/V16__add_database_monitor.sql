-- 数据库监控仅对显式授予查询权限的普通角色及超级管理员可见。
INSERT INTO sys_permission
    (id, parent_id, route_name, menu_name, perms, menu_type, path, component, is_cache, visible, sort, icon, built_in)
VALUES
    (4500, 300, 'MonitorDatabase', '数据库监控', 'monitor:database:query', 2, '/monitor/database', 'monitor/database/index', 0, 0, 4, 'database', 1);
