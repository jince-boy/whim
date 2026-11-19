-- 菜单树按 parent_id 构建，将整个日志目录移到系统管理下。
UPDATE sys_permission
SET parent_id = 100
WHERE id = 200 AND deleted = 0 AND parent_id <> 100;
