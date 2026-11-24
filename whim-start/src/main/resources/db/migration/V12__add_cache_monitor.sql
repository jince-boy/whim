-- 缓存监控为只读菜单，普通角色须显式授权。
INSERT INTO sys_permission
    (id, parent_id, route_name, menu_name, perms, menu_type, path, component, is_cache, visible, sort, icon, built_in)
VALUES
    (4200, 300, 'MonitorCache', '缓存监控', 'monitor:cache:query', 2, '/monitor/cache', 'monitor/cache/index', 0, 0, 1, 'database', 1);
