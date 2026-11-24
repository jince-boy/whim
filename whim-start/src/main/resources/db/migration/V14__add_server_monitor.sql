-- 普通角色须显式授予服务器监控的查询权限。
INSERT INTO sys_permission
    (id, parent_id, route_name, menu_name, perms, menu_type, path, component, is_cache, visible, sort, icon, built_in)
VALUES
    (4300, 300, 'MonitorServer', '服务器监控', 'monitor:server:query', 2, '/monitor/server', 'monitor/server/index', 0, 0, 2, 'server', 1);
