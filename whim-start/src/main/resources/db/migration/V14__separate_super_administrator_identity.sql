-- 超级管理员是账号身份，不再依赖角色或逐项菜单授权；业务DTO不能维护该字段。
ALTER TABLE sys_user
    ADD COLUMN super_administrator TINYINT NOT NULL DEFAULT 0 COMMENT '超级管理员身份：0否、1是' AFTER username,
    ADD CONSTRAINT chk_sys_user_super_administrator CHECK (super_administrator IN (0, 1));

-- 保留旧版有效超级管理员角色成员的身份，不依赖固定用户名或用户ID。
UPDATE sys_user account
INNER JOIN sys_user_role membership ON membership.user_id = account.id
INNER JOIN sys_role legacy_role ON legacy_role.id = membership.role_id
    AND legacy_role.deleted = 0 AND legacy_role.status = 0
    AND CAST(legacy_role.role_code AS BINARY) = CAST('superadmin' AS BINARY)
SET account.super_administrator = 1
WHERE account.deleted = 0;

-- 清理旧超级管理员角色的关联，普通角色及其授权保持原样。
DELETE membership FROM sys_user_role membership
INNER JOIN sys_role legacy_role ON legacy_role.id = membership.role_id
WHERE legacy_role.deleted = 0 AND CAST(legacy_role.role_code AS BINARY) = CAST('superadmin' AS BINARY);

DELETE relation FROM sys_role_permission relation
INNER JOIN sys_role legacy_role ON legacy_role.id = relation.role_id
WHERE legacy_role.deleted = 0 AND CAST(legacy_role.role_code AS BINARY) = CAST('superadmin' AS BINARY);

DELETE relation FROM sys_role_dept relation
INNER JOIN sys_role legacy_role ON legacy_role.id = relation.role_id
WHERE legacy_role.deleted = 0 AND CAST(legacy_role.role_code AS BINARY) = CAST('superadmin' AS BINARY);

-- 旧角色真正逻辑删除，角色列表和候选项无需使用固定ID或编码过滤。
UPDATE sys_role SET deleted = id, delete_time = CURRENT_TIMESTAMP(3)
WHERE deleted = 0 AND CAST(role_code AS BINARY) = CAST('superadmin' AS BINARY);
