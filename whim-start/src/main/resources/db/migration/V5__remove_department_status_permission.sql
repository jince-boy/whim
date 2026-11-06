-- 部门状态统一通过修改表单维护，移除独立状态操作的权限及角色授权。
DELETE role_permission
FROM sys_role_permission role_permission
INNER JOIN sys_permission permission ON permission.id = role_permission.permission_id
WHERE permission.perms = 'system:dept:status';

UPDATE sys_permission
SET deleted = id, delete_time = CURRENT_TIMESTAMP(3)
WHERE deleted = 0
  AND perms = 'system:dept:status';
