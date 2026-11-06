-- 用户最多归属一个主部门，允许尚未分配部门；保留现有外键与索引。
ALTER TABLE sys_user MODIFY COLUMN dept_id BIGINT NULL COMMENT '主部门ID，空表示未分配部门';

-- 性别选项由真实字典维护，前端不硬编码标签。
INSERT INTO sys_dict_type (id, name, type, remark, create_by)
SELECT 2004, '用户性别', 'sys_user_gender', '用户性别：0未知，1男，2女', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE type = 'sys_user_gender' AND deleted = 0);

INSERT INTO sys_dict_data (id, type, label, value, list_class, sort, create_by)
SELECT 20040, 'sys_user_gender', '未知', '0', 'default', 0, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE type = 'sys_user_gender' AND value = '0' AND deleted = 0);
INSERT INTO sys_dict_data (id, type, label, value, list_class, sort, create_by)
SELECT 20041, 'sys_user_gender', '男', '1', 'info', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE type = 'sys_user_gender' AND value = '1' AND deleted = 0);
INSERT INTO sys_dict_data (id, type, label, value, list_class, sort, create_by)
SELECT 20042, 'sys_user_gender', '女', '2', 'warning', 2, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE type = 'sys_user_gender' AND value = '2' AND deleted = 0);

-- 部门和状态通过用户编辑维护，不保留没有独立接口的权限入口。
DELETE role_permission
FROM sys_role_permission role_permission
INNER JOIN sys_permission permission ON permission.id = role_permission.permission_id
WHERE permission.perms IN ('system:user:department', 'system:user:status');

UPDATE sys_permission
SET deleted = id, delete_time = CURRENT_TIMESTAMP(3)
WHERE deleted = 0
  AND perms IN ('system:user:department', 'system:user:status');
