-- 首次初始化平台管理员；已有 admin 账号时不覆盖其密码或授权。
SET @whim_create_admin = (SELECT COUNT(*) = 0 FROM sys_user WHERE username = 'admin');
SET @whim_admin_id = (SELECT COALESCE(MAX(id), 0) + 1 FROM sys_user);

INSERT INTO sys_user (id, username, password, name, status, deleted, create_time)
SELECT @whim_admin_id,
       'admin',
       '$2a$10$1MICRXF1CEQGu8THNJ5.k.8k6CzyoQ9254RGNv7XfMTd06B02LKAW',
       '平台管理员',
       0,
       0,
       CURRENT_TIMESTAMP
WHERE @whim_create_admin = 1;

-- 复用现有的有效平台超级管理员角色，否则创建平台角色。
SET @whim_superadmin_role_id = (
    SELECT id
    FROM sys_role
    WHERE role_code = 'superadmin'
      AND role_type = 0
      AND tenant_id IS NULL
      AND status = 0
      AND deleted = 0
    ORDER BY id
    LIMIT 1
);
SET @whim_create_superadmin_role = IF(
    @whim_create_admin = 1 AND @whim_superadmin_role_id IS NULL,
    1,
    0
);
SET @whim_superadmin_role_id = COALESCE(
    @whim_superadmin_role_id,
    (SELECT COALESCE(MAX(id), 0) + 1 FROM sys_role)
);

INSERT INTO sys_role
    (id, role_name, role_code, role_type, data_scope, sort, tenant_id, status, deleted, create_time)
SELECT @whim_superadmin_role_id,
       '平台超级管理员',
       'superadmin',
       0,
       1,
       0,
       NULL,
       0,
       0,
       CURRENT_TIMESTAMP
WHERE @whim_create_superadmin_role = 1;

SET @whim_admin_role_binding_id = (SELECT COALESCE(MAX(id), 0) + 1 FROM sys_user_role);

INSERT INTO sys_user_role (id, user_id, role_id, tenant_id, deleted, create_time)
SELECT @whim_admin_role_binding_id,
       @whim_admin_id,
       @whim_superadmin_role_id,
       NULL,
       0,
       CURRENT_TIMESTAMP
WHERE @whim_create_admin = 1;
