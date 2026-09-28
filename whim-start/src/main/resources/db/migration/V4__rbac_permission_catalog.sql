-- 功能权限码目录。只补缺失项，不覆盖已有菜单配置或授权关系。
INSERT INTO sys_permission
    (id, route_name, menu_name, perms, parent_id, menu_type, sort, status, deleted, create_time)
SELECT maximum_id.max_id + ROW_NUMBER() OVER (ORDER BY catalog.perms),
       catalog.perms,
       catalog.menu_name,
       catalog.perms,
       0,
       3,
       0,
       0,
       0,
       CURRENT_TIMESTAMP
FROM (
         SELECT 'system:member:list' AS perms, '查看租户成员' AS menu_name
         UNION ALL SELECT 'system:member:status', '修改租户成员状态'
         UNION ALL SELECT 'system:role:list', '查看租户角色'
         UNION ALL SELECT 'system:role:create', '创建租户角色'
         UNION ALL SELECT 'system:role:update', '修改租户角色'
         UNION ALL SELECT 'system:role:status', '修改租户角色状态'
         UNION ALL SELECT 'system:userRole:list', '查看成员角色'
         UNION ALL SELECT 'system:userRole:assign', '分配成员角色'
         UNION ALL SELECT 'system:permission:list', '查看可分配权限'
         UNION ALL SELECT 'system:rolePermission:list', '查看角色权限'
         UNION ALL SELECT 'system:rolePermission:assign', '分配角色权限'
         UNION ALL SELECT 'system:platform:permission:list', '查看平台权限目录'
         UNION ALL SELECT 'system:platform:user:status', '修改全局用户状态'
         UNION ALL SELECT 'system:platform:tenant:status', '修改租户状态'
         UNION ALL SELECT 'system:platform:tenant:package', '修改租户套餐'
         UNION ALL SELECT 'system:platform:package:status', '修改套餐状态'
         UNION ALL SELECT 'system:platform:package:assign', '分配套餐权限'
         UNION ALL SELECT 'system:platform:permission:status', '修改全局权限状态'
     ) catalog
         CROSS JOIN (SELECT COALESCE(MAX(id), 0) AS max_id FROM sys_permission) maximum_id
WHERE NOT EXISTS (SELECT 1 FROM sys_permission existing WHERE existing.perms = catalog.perms);
