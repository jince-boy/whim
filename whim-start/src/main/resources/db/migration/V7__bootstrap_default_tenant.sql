-- 尚无租户的安装预置一个默认租户。单租户项目继续使用同一套租户作用域 RBAC，登录后无需手动选租户。
-- 已有租户或同名套餐时不接管旧数据；只有有效平台超级管理员 admin 才参与初始化。
SET @whim_default_admin_id = (
    SELECT u.id
    FROM sys_user u
             INNER JOIN sys_user_role ur
                        ON ur.user_id = u.id AND ur.tenant_id IS NULL AND ur.deleted = 0
             INNER JOIN sys_role r
                        ON r.id = ur.role_id AND r.tenant_id IS NULL
                            AND r.role_type = 0 AND r.role_code = 'superadmin'
                            AND r.status = 0 AND r.deleted = 0
    WHERE u.username = 'admin' AND u.status = 0 AND u.deleted = 0
    LIMIT 1
);
SET @whim_create_default_tenant = IF(
    @whim_default_admin_id IS NOT NULL
        AND NOT EXISTS (SELECT 1 FROM sys_tenant)
        AND NOT EXISTS (SELECT 1 FROM sys_tenant_package WHERE package_name = 'Whim 默认套餐'),
    1,
    0
);

SET @whim_default_package_id = (SELECT COALESCE(MAX(id), 0) + 1 FROM sys_tenant_package);
SET @whim_default_tenant_id = (SELECT COALESCE(MAX(id), 0) + 1 FROM sys_tenant);
SET @whim_default_role_id = (SELECT COALESCE(MAX(id), 0) + 1 FROM sys_role);
SET @whim_default_member_id = (SELECT COALESCE(MAX(id), 0) + 1 FROM sys_user_tenant);
SET @whim_default_role_binding_id = (SELECT COALESCE(MAX(id), 0) + 1 FROM sys_user_role);

INSERT INTO sys_tenant_package
    (id, package_name, sort, status, remark, deleted, create_time)
SELECT @whim_default_package_id, 'Whim 默认套餐', 0, 0,
       '全新安装的默认租户套餐；仅包含当前已初始化的租户功能权限', 0, CURRENT_TIMESTAMP
WHERE @whim_create_default_tenant = 1;

INSERT INTO sys_tenant
    (id, user_id, tenant_code, company_name, contact_person, contact_number,
     package_id, status, account_count, remark, deleted, create_time)
SELECT @whim_default_tenant_id, @whim_default_admin_id, 'default', '默认租户',
       '系统管理员', '未设置', @whim_default_package_id, 0, 0,
       '脚手架默认租户；多租户项目可继续创建其他租户', 0, CURRENT_TIMESTAMP
WHERE @whim_create_default_tenant = 1;

INSERT INTO sys_user_tenant
    (id, user_id, tenant_id, status, deleted, create_time)
SELECT @whim_default_member_id,
       @whim_default_admin_id, @whim_default_tenant_id, 0, 0, CURRENT_TIMESTAMP
WHERE @whim_create_default_tenant = 1;

INSERT INTO sys_role
    (id, role_name, role_code, role_type, data_scope, sort, tenant_id, status, remark, deleted, create_time)
SELECT @whim_default_role_id, '默认租户管理员', 'tenant_admin', 1, 1, 0,
       @whim_default_tenant_id, 0, '默认租户初始管理角色', 0, CURRENT_TIMESTAMP
WHERE @whim_create_default_tenant = 1;

INSERT INTO sys_user_role
    (id, user_id, role_id, tenant_id, deleted, create_time)
SELECT @whim_default_role_binding_id,
       @whim_default_admin_id, @whim_default_role_id,
       @whim_default_tenant_id, 0, CURRENT_TIMESTAMP
WHERE @whim_create_default_tenant = 1;

SET @whim_package_permission_max_id =
    (SELECT COALESCE(MAX(id), 0) FROM sys_tenant_package_permission);
INSERT INTO sys_tenant_package_permission
    (id, package_id, permission_id, deleted, create_time)
SELECT @whim_package_permission_max_id + ROW_NUMBER() OVER (ORDER BY permission.id),
       @whim_default_package_id, permission.id, 0, CURRENT_TIMESTAMP
FROM sys_permission permission
WHERE @whim_create_default_tenant = 1
  AND permission.deleted = 0
  AND permission.status = 0
  AND permission.perms LIKE 'system:%'
  AND permission.perms NOT LIKE 'system:platform:%';

SET @whim_role_permission_max_id = (SELECT COALESCE(MAX(id), 0) FROM sys_role_permission);
INSERT INTO sys_role_permission
    (id, role_id, permission_id, tenant_id, deleted, create_time)
SELECT @whim_role_permission_max_id + ROW_NUMBER() OVER (ORDER BY permission.id),
       @whim_default_role_id, permission.id, @whim_default_tenant_id, 0, CURRENT_TIMESTAMP
FROM sys_permission permission
WHERE @whim_create_default_tenant = 1
  AND permission.deleted = 0
  AND permission.status = 0
  AND permission.perms LIKE 'system:%'
  AND permission.perms NOT LIKE 'system:platform:%';

UPDATE sys_user
SET default_tenant_id = @whim_default_tenant_id,
    update_time = CURRENT_TIMESTAMP
WHERE @whim_create_default_tenant = 1
  AND id = @whim_default_admin_id
  AND default_tenant_id IS NULL;
