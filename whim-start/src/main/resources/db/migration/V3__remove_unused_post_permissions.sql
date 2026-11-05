-- 岗位状态统一通过修改岗位维护，成员分配由用户管理负责。
-- 删除失效权限的角色关联，不将原状态权限升级为完整修改权限。
DELETE role_permission
FROM sys_role_permission role_permission
INNER JOIN sys_permission permission ON permission.id = role_permission.permission_id
WHERE permission.perms IN ('system:post:status', 'system:post:member:assign');

UPDATE sys_permission
SET deleted = id, delete_time = CURRENT_TIMESTAMP(3)
WHERE deleted = 0
  AND perms IN ('system:post:status', 'system:post:member:assign');
