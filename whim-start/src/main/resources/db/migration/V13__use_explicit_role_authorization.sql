-- 仅为旧版本依靠角色编码自动拥有全部能力的账号补齐迁移时已有的显式菜单授权。
-- 本脚本只执行一次；新增菜单此后必须通过角色权限管理主动授予。
INSERT INTO sys_role_permission (id, role_id, permission_id, create_by)
SELECT identifiers.base_id + ROW_NUMBER() OVER (ORDER BY legacy_role.id, permission.id),
       legacy_role.id, permission.id, legacy_role.create_by
FROM sys_role legacy_role
CROSS JOIN sys_permission permission
CROSS JOIN (
    SELECT GREATEST(COALESCE(MAX(id), 0), 2000000000) AS base_id FROM sys_role_permission
) identifiers
LEFT JOIN sys_role_permission relation
       ON relation.role_id = legacy_role.id AND relation.permission_id = permission.id
WHERE legacy_role.deleted = 0 AND legacy_role.role_code = 'superadmin'
  AND permission.deleted = 0 AND relation.id IS NULL;

-- 旧版本的全部数据能力也落为普通配置，运行时不再按角色名称放行。
UPDATE sys_role SET data_scope = 1 WHERE deleted = 0 AND role_code = 'superadmin';
