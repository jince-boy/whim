-- 普通角色须显式授予页面和强退权限，超级管理员沿用独立身份授权。
INSERT INTO sys_permission
    (id, parent_id, route_name, menu_name, perms, menu_type, path, component, is_cache, visible, sort, icon, built_in)
VALUES
    (300, 0, 'Monitor', '系统监控', NULL, 1, '/monitor', NULL, 0, 0, 10, 'activity', 1),
    (4100, 300, 'MonitorOnlineUser', '在线用户', 'monitor:onlineUser:page', 2, '/monitor/onlineUser', 'monitor/onlineUser/index', 0, 0, 0, 'users', 1);

INSERT INTO sys_permission (id, parent_id, menu_name, perms, menu_type, sort, built_in) VALUES
    (4101, 4100, '强制下线', 'monitor:onlineUser:kickout', 3, 0, 1);
