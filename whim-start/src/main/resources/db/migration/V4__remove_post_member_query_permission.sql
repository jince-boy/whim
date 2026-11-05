-- 岗位管理仅维护岗位信息，移除已下线的岗位成员查询权限及角色授权。
DELETE role_permission
FROM sys_role_permission role_permission
INNER JOIN sys_permission permission ON permission.id = role_permission.permission_id
WHERE permission.perms = 'system:post:member:list';

UPDATE sys_permission
SET deleted = id, delete_time = CURRENT_TIMESTAMP(3)
WHERE deleted = 0
  AND perms = 'system:post:member:list';
