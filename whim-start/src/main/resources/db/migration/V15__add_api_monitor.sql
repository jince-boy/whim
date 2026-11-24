-- 普通角色须显式授予接口监控的查询权限。
INSERT INTO sys_permission
    (id, parent_id, route_name, menu_name, perms, menu_type, path, component, is_cache, visible, sort, icon, built_in)
VALUES
    (4400, 300, 'MonitorApi', '接口监控', 'monitor:api:query', 2, '/monitor/api', 'monitor/api/index', 0, 0, 3, 'activity', 1);
