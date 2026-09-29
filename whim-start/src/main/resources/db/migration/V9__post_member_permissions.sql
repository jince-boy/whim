-- 岗位成员关系的查看与分配使用独立的动作权限。
INSERT INTO sys_permission
    (id, route_name, menu_name, perms, parent_id, menu_type, sort, status, deleted, create_time)
SELECT maximum_id.max_id + ROW_NUMBER() OVER (ORDER BY catalog.perms),
       catalog.perms, catalog.menu_name, catalog.perms, 0, 3, 0, 0, 0, CURRENT_TIMESTAMP
FROM (
         SELECT 'system:post:member:list' AS perms, '查看岗位成员' AS menu_name
         UNION ALL SELECT 'system:post:member:assign', '分配岗位成员'
     ) catalog
         CROSS JOIN (SELECT COALESCE(MAX(id), 0) AS max_id FROM sys_permission) maximum_id
WHERE NOT EXISTS (SELECT 1 FROM sys_permission existing WHERE existing.perms = catalog.perms);

-- 只向脚手架初始化的默认租户延续内置授权。
SET @whim_seed_tenant_id = (
    SELECT id FROM sys_tenant
    WHERE tenant_code = 'default'
      AND remark = '脚手架默认租户；多租户项目可继续创建其他租户'
      AND status = 0 AND deleted = 0
    LIMIT 1
);
SET @whim_seed_package_id = (
    SELECT p.id FROM sys_tenant_package p
                         INNER JOIN sys_tenant t ON t.package_id = p.id
    WHERE t.id = @whim_seed_tenant_id
      AND p.package_name = 'Whim 默认套餐' AND p.status = 0 AND p.deleted = 0
    LIMIT 1
);
SET @whim_seed_role_id = (
    SELECT id FROM sys_role
    WHERE tenant_id = @whim_seed_tenant_id AND role_code = 'tenant_admin'
      AND role_type = 1 AND remark = '默认租户初始管理角色'
      AND status = 0 AND deleted = 0
    LIMIT 1
);

SET @whim_package_permission_max_id =
    (SELECT COALESCE(MAX(id), 0) FROM sys_tenant_package_permission);
INSERT INTO sys_tenant_package_permission
    (id, package_id, permission_id, deleted, create_time)
SELECT @whim_package_permission_max_id + ROW_NUMBER() OVER (ORDER BY permission.id),
       @whim_seed_package_id, permission.id, 0, CURRENT_TIMESTAMP
FROM sys_permission permission
WHERE @whim_seed_package_id IS NOT NULL
  AND permission.perms IN ('system:post:member:list', 'system:post:member:assign')
  AND permission.status = 0 AND permission.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_tenant_package_permission existing
      WHERE existing.package_id = @whim_seed_package_id AND existing.permission_id = permission.id
  );

SET @whim_role_permission_max_id = (SELECT COALESCE(MAX(id), 0) FROM sys_role_permission);
INSERT INTO sys_role_permission
    (id, role_id, permission_id, tenant_id, deleted, create_time)
SELECT @whim_role_permission_max_id + ROW_NUMBER() OVER (ORDER BY permission.id),
       @whim_seed_role_id, permission.id, @whim_seed_tenant_id, 0, CURRENT_TIMESTAMP
FROM sys_permission permission
WHERE @whim_seed_role_id IS NOT NULL AND @whim_seed_package_id IS NOT NULL
  AND permission.perms IN ('system:post:member:list', 'system:post:member:assign')
  AND permission.status = 0 AND permission.deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_permission existing
      WHERE existing.role_id = @whim_seed_role_id AND existing.permission_id = permission.id
        AND existing.tenant_id = @whim_seed_tenant_id
  );
