-- 平台角色及其权限关系使用 NULL tenant_id；删除同一用户/角色或角色/权限的历史重复绑定。
-- 重复记录只保留一条：优先保留未删除记录，其次保留最小 ID。
-- 删除前将冗余关系原样保存在迁移档案表，便于人工核对历史归属。
CREATE TABLE sys_rbac_migration_user_role_archive LIKE sys_user_role;
CREATE TABLE sys_rbac_migration_role_permission_archive LIKE sys_role_permission;

-- 错租户的历史关系不允许在新模型下继续生效，先存档再移除。
INSERT INTO sys_rbac_migration_user_role_archive
SELECT binding.*
FROM sys_user_role binding
         INNER JOIN sys_role assigned_role ON assigned_role.id = binding.role_id
WHERE assigned_role.tenant_id IS NOT NULL
  AND assigned_role.tenant_id != binding.tenant_id;

DELETE binding
FROM sys_user_role binding
         INNER JOIN sys_role assigned_role ON assigned_role.id = binding.role_id
WHERE assigned_role.tenant_id IS NOT NULL
  AND assigned_role.tenant_id != binding.tenant_id;

INSERT INTO sys_rbac_migration_role_permission_archive
SELECT binding.*
FROM sys_role_permission binding
         INNER JOIN sys_role assigned_role ON assigned_role.id = binding.role_id
WHERE assigned_role.tenant_id IS NOT NULL
  AND assigned_role.tenant_id != binding.tenant_id;

DELETE binding
FROM sys_role_permission binding
         INNER JOIN sys_role assigned_role ON assigned_role.id = binding.role_id
WHERE assigned_role.tenant_id IS NOT NULL
  AND assigned_role.tenant_id != binding.tenant_id;

INSERT INTO sys_rbac_migration_user_role_archive
SELECT DISTINCT duplicate_binding.*
FROM sys_user_role duplicate_binding
         INNER JOIN sys_user_role retained_binding
                    ON retained_binding.user_id = duplicate_binding.user_id
                        AND retained_binding.role_id = duplicate_binding.role_id
                        AND (retained_binding.deleted < duplicate_binding.deleted
                            OR (retained_binding.deleted = duplicate_binding.deleted
                                AND retained_binding.id < duplicate_binding.id));

INSERT INTO sys_rbac_migration_role_permission_archive
SELECT DISTINCT duplicate_binding.*
FROM sys_role_permission duplicate_binding
         INNER JOIN sys_role_permission retained_binding
                    ON retained_binding.role_id = duplicate_binding.role_id
                        AND retained_binding.permission_id = duplicate_binding.permission_id
                        AND (retained_binding.deleted < duplicate_binding.deleted
                            OR (retained_binding.deleted = duplicate_binding.deleted
                                AND retained_binding.id < duplicate_binding.id));

DELETE duplicate_binding
FROM sys_user_role duplicate_binding
         INNER JOIN sys_user_role retained_binding
                    ON retained_binding.user_id = duplicate_binding.user_id
                        AND retained_binding.role_id = duplicate_binding.role_id
                        AND (retained_binding.deleted < duplicate_binding.deleted
                            OR (retained_binding.deleted = duplicate_binding.deleted
                                AND retained_binding.id < duplicate_binding.id));

DELETE duplicate_binding
FROM sys_role_permission duplicate_binding
         INNER JOIN sys_role_permission retained_binding
                    ON retained_binding.role_id = duplicate_binding.role_id
                        AND retained_binding.permission_id = duplicate_binding.permission_id
                        AND (retained_binding.deleted < duplicate_binding.deleted
                            OR (retained_binding.deleted = duplicate_binding.deleted
                                AND retained_binding.id < duplicate_binding.id));

ALTER TABLE sys_user_role
    MODIFY COLUMN tenant_id bigint(20) NULL DEFAULT NULL COMMENT '租户ID，平台角色为空',
    DROP INDEX uk_user_role_tenant,
    ADD UNIQUE KEY uk_user_role (user_id, role_id);

ALTER TABLE sys_role_permission
    MODIFY COLUMN tenant_id bigint(20) NULL DEFAULT NULL COMMENT '租户ID，平台角色为空',
    DROP INDEX uk_role_permission_tenant,
    ADD UNIQUE KEY uk_role_permission (role_id, permission_id);

UPDATE sys_user_role binding
    INNER JOIN sys_role assigned_role ON assigned_role.id = binding.role_id
SET binding.tenant_id = NULL
WHERE assigned_role.tenant_id IS NULL;

UPDATE sys_role_permission binding
    INNER JOIN sys_role assigned_role ON assigned_role.id = binding.role_id
SET binding.tenant_id = NULL
WHERE assigned_role.tenant_id IS NULL;

ALTER TABLE sys_user_tenant
    ADD COLUMN status tinyint(1) NOT NULL DEFAULT 0 COMMENT '成员状态(0正常 1停用)' AFTER tenant_id,
    ADD KEY idx_user_tenant_tenant_status (tenant_id, status, deleted, user_id);
