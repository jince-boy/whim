-- 角色用户管理使用独立的功能权限；普通角色需显式授权，不沿用用户管理的权限范围。
INSERT INTO sys_permission
    (id, menu_name, perms, parent_id, menu_type, sort, built_in)
VALUES
    (1110, '角色用户查询', 'system:roleUser:list', 1100, 3, 10, 1),
    (1111, '角色用户分配', 'system:roleUser:assign', 1100, 3, 11, 1);
