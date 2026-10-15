-- 首页与其他页面共用菜单授权，不在菜单表定义用户或角色的首页分配规则。
INSERT INTO sys_permission
    (id, route_name, menu_name, perms, parent_id, menu_type, path, component,
     is_cache, sort, icon, built_in, remark, create_by)
VALUES
    (90, 'home', '首页', 'system:home:view', 0, 2, '/index', 'layout/default/index',
     1, 0, 'smile', 1, '默认首页组件，首页分配由独立配置维护', 1);

-- 与其他内置页面一致，默认授予超级管理员；普通角色显式授予此页面。
INSERT INTO sys_role_permission (id, role_id, permission_id, create_by)
VALUES (41, 1, 90, 1);
