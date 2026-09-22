-- 为用户增加默认进入租户配置
SET @default_tenant_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND column_name = 'default_tenant_id'
);

SET @add_default_tenant_column_sql = IF(
    @default_tenant_column_exists = 0,
    'ALTER TABLE sys_user ADD COLUMN default_tenant_id bigint(20) NULL DEFAULT NULL COMMENT ''默认进入租户ID'' AFTER status',
    'SELECT 1'
);

PREPARE add_default_tenant_column_statement FROM @add_default_tenant_column_sql;
EXECUTE add_default_tenant_column_statement;
DEALLOCATE PREPARE add_default_tenant_column_statement;
